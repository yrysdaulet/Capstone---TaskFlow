package com.epam.taskflow.controller;

import com.epam.taskflow.dto.CardDTO;
import com.epam.taskflow.model.User;
import com.epam.taskflow.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor

public class CardController {

    private final CardService cardService;
    @PostMapping

    public ResponseEntity<CardDTO> createCard(@RequestBody CardDTO cardDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();

        CardDTO createdCard = cardService.createCard(cardDTO, userId);
        return ResponseEntity.ok(createdCard);
    }

    @GetMapping("/list/{listId}")
    public ResponseEntity<List<CardDTO>> getAllCardsForList(@PathVariable Long listId,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        List<CardDTO> cards = cardService.getAllCardsForList(listId, userId);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardDTO> updateCard(@PathVariable Long id,
                                              @RequestBody CardDTO cardDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        CardDTO updatedCard = cardService.updateCard(id, cardDTO, userId);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        cardService.deleteCard(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<Void> moveCard(@PathVariable Long id,
                                         @RequestParam Long newListId,
                                         @RequestParam(required = false) Integer newPosition,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        cardService.moveCard(id, newListId, newPosition, userId);
        return ResponseEntity.noContent().build();
    }
}