package com.epam.taskflow.controller;

import com.epam.taskflow.dto.ListDTO;
import com.epam.taskflow.model.User;
import com.epam.taskflow.service.ListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor

public class ListController {

    private final ListService listService;

    @PostMapping
    public ResponseEntity<ListDTO> createList(@RequestBody ListDTO listDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        ListDTO createdList = listService.createList(listDTO, userId);
        return ResponseEntity.ok(createdList);
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<ListDTO>> getAllListsForBoard(@PathVariable Long boardId,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        List<ListDTO> lists = listService.getAllListsForBoard(boardId, userId);
        return ResponseEntity.ok(lists);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListDTO> updateList(@PathVariable Long id,
                                              @RequestBody ListDTO listDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        ListDTO updatedList = listService.updateList(id, listDTO, userId);
        return ResponseEntity.ok(updatedList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteList(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        listService.deleteList(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<Void> moveList(@PathVariable Long id,
                                         @RequestParam Integer newPosition,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        listService.moveList(id, newPosition, userId);
        return ResponseEntity.noContent().build();
    }
}