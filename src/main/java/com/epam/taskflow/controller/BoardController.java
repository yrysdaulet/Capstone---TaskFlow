package com.epam.taskflow.controller;

import com.epam.taskflow.dto.BoardDTO;
import com.epam.taskflow.model.User;
import com.epam.taskflow.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor

public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<BoardDTO> createBoard(@Valid @RequestBody BoardDTO boardDTO,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        
        BoardDTO createdBoard = boardService.createBoard(boardDTO, userId);
        return ResponseEntity.status(201).body(createdBoard);
    }

    @GetMapping
    public ResponseEntity<List<BoardDTO>> getAllBoards(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        List<BoardDTO> boards = boardService.getAllBoardsForUser(userId);
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardDTO> getBoardById(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        BoardDTO board = boardService.getBoardById(id, userId);
        return ResponseEntity.ok(board);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardDTO> updateBoard(@PathVariable Long id,
                                               @jakarta.validation.Valid @RequestBody BoardDTO boardDTO,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        BoardDTO updatedBoard = boardService.updateBoard(id, boardDTO, userId);
        return ResponseEntity.ok(updatedBoard);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        boardService.deleteBoard(id, userId);
        return ResponseEntity.noContent().build();
    }
}