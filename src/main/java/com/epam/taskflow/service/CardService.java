package com.epam.taskflow.service;

import com.epam.taskflow.dto.CardDTO;
import com.epam.taskflow.exception.ResourceNotFoundException;
import com.epam.taskflow.exception.UnauthorizedAccessException;
import com.epam.taskflow.model.Board;
import com.epam.taskflow.model.Card;
import com.epam.taskflow.model.ListEntity;
import com.epam.taskflow.model.User;
import com.epam.taskflow.repository.CardRepository;
import com.epam.taskflow.repository.ListRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final ListRepository listRepository;
    private final BoardService boardService;
    private final ModelMapper modelMapper;
    private final PermissionService permissionService;

    @Transactional
    public CardDTO createCard(CardDTO cardDTO, Long userId) {
        ListEntity list = listRepository.findById(cardDTO.getListId())
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));

        if (!boardService.isUserAuthorized(list.getBoard(), userId)) {
            throw new UnauthorizedAccessException("User not authorized to add cards to this list");
        }
        Board board = list.getBoard();
        permissionService.validateEditorAccess(board, userId);

        Card card = modelMapper.map(cardDTO, Card.class);
        card.setList(list);

        // Set position to the end if not provided
        if (card.getPosition() == null) {
            Integer maxPosition = cardRepository.findMaxPositionByList(list);
            card.setPosition(maxPosition != null ? maxPosition + 1 : 0);
        }

        Card savedCard = cardRepository.save(card);
        return modelMapper.map(savedCard, CardDTO.class);
    }

    public List<CardDTO> getAllCardsForList(Long listId, Long userId) {
        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));

        if (!boardService.isUserAuthorized(((ListEntity) list).getBoard(), userId)) {
            throw new UnauthorizedAccessException("User not authorized to view cards in this list");
        }
        Board board = list.getBoard();
        permissionService.validateEditorAccess(board, userId);

        return cardRepository.findByListOrderByPositionAsc(list).stream()
                .map(card -> modelMapper.map(card, CardDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public CardDTO updateCard(Long cardId, CardDTO cardDTO, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        if (!boardService.isUserAuthorized(card.getList().getBoard(), userId)) {
            throw new UnauthorizedAccessException("User not authorized to update this card");
        }
        Board board = card.getList().getBoard();
        permissionService.validateEditorAccess(board, userId);
        card.setTitle(cardDTO.getTitle());
        card.setDescription(cardDTO.getDescription());
        card.setDueDate(cardDTO.getDueDate());

        Card updatedCard = cardRepository.save(card);
        return modelMapper.map(updatedCard, CardDTO.class);
    }

    @Transactional
    public void deleteCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        if (!boardService.isUserAuthorized(card.getList().getBoard(), userId)) {
            throw new UnauthorizedAccessException("User not authorized to delete this card");
        }
        Board board = card.getList().getBoard();

        permissionService.validateEditorAccess(board, userId);

        cardRepository.delete(card);
    }

    @Transactional
    public void moveCard(Long cardId, Long newListId, Integer newPosition, Long userId) {

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        ListEntity currentList = card.getList();
        Board currentBoard = currentList.getBoard();
        permissionService.validateEditorAccess(currentBoard, userId);


        if (!boardService.isUserAuthorized(currentBoard, userId)) {
            throw new UnauthorizedAccessException("User not authorized to move this card");
        }

        ListEntity newList = listRepository.findById(newListId)
                .orElseThrow(() -> new ResourceNotFoundException("New list not found"));

        if (!boardService.isUserAuthorized(newList.getBoard(), userId)) {
            throw new UnauthorizedAccessException("User not authorized to move card to this list");
        }

        List<Card> currentCards = cardRepository.findByListOrderByPositionAsc(currentList);
        currentCards.removeIf(c -> c.getId().equals(cardId));

        for (int i = 0; i < currentCards.size(); i++) {
            currentCards.get(i).setPosition(i);
        }

        List<Card> newCards = cardRepository.findByListOrderByPositionAsc(newList);

        if (newPosition == null || newPosition < 0 || newPosition > newCards.size()) {
            newPosition = newCards.size(); // Move to end if position is invalid
        }

        card.setList(newList);
        newCards.add(newPosition, card);

        for (int i = 0; i < newCards.size(); i++) {
            newCards.get(i).setPosition(i);
        }

        cardRepository.saveAll(currentCards);
        cardRepository.saveAll(newCards);
    }
}