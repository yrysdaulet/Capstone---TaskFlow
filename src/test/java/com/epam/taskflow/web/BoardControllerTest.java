package com.epam.taskflow.web;

import com.epam.taskflow.controller.BoardController;
import com.epam.taskflow.dto.BoardDTO;
import com.epam.taskflow.model.User;
import com.epam.taskflow.security.JwtRequestFilter;
import com.epam.taskflow.service.BoardService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BoardController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class BoardControllerTest {

    @TestConfiguration
    static class Stubs {
        @Bean
        BoardService boardService() { return Mockito.mock(BoardService.class); }
        @Bean
        JwtRequestFilter jwtRequestFilter() { return new JwtRequestFilter(null, null) {
            @Override
            protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                                            jakarta.servlet.http.HttpServletResponse response,
                                            jakarta.servlet.FilterChain filterChain) throws java.io.IOException, jakarta.servlet.ServletException {
                // bypass authentication in slice tests but continue the chain
                filterChain.doFilter(request, response);
            }
        }; }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardService boardService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    private void authenticateAs(Long userId, String username) {
        User principal = User.builder()
                .id(userId)
                .username(username)
                .email(username+"@example.com")
                .password("pwd")
                .build();
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createBoard_returns201_and_body() throws Exception {
        authenticateAs(1L, "demo");
        Mockito.when(boardService.createBoard(any(BoardDTO.class), eq(1L)))
                .thenAnswer(inv -> {
                    BoardDTO in = inv.getArgument(0);
                    return BoardDTO.builder().id(10L).title(in.getTitle()).ownerId(1L).build();
                });

        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My Board\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.title", is("My Board")));
    }

    @Test
    void createBoard_validationFailure_returns400() throws Exception {
        authenticateAs(1L, "demo");
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
