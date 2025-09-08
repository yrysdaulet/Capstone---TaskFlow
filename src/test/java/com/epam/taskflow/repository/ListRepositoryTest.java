package com.epam.taskflow.repository;

import com.epam.taskflow.model.Board;
import com.epam.taskflow.model.ListEntity;
import com.epam.taskflow.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ListRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ListRepository listRepository;

    @Test
    void findByBoardOrderByPositionAsc_and_findMaxPositionByBoard() {

        User user = User.builder()
                .username("repo_user")
                .email("repo_user@example.com")
                .password("pwd")
                .build();
        em.persist(user);

        Board board = Board.builder()
                .title("Repo Test Board")
                .owner(user)
                .build();
        em.persist(board);

        ListEntity l0 = ListEntity.builder().title("A").position(0).board(board).build();
        ListEntity l2 = ListEntity.builder().title("B").position(2).board(board).build();
        ListEntity l1 = ListEntity.builder().title("C").position(1).board(board).build();
        em.persist(l0);
        em.persist(l2);
        em.persist(l1);
        em.flush();

        List<ListEntity> ordered = listRepository.findByBoardOrderByPositionAsc(board);
        Integer max = listRepository.findMaxPositionByBoard(board);

        assertThat(ordered).extracting(ListEntity::getPosition).containsExactly(0, 1, 2);
        assertThat(max).isEqualTo(2);
    }
}
