package com.epam.taskflow.service;

import com.epam.taskflow.dto.ListDTO;
import com.epam.taskflow.exception.ResourceNotFoundException;
import com.epam.taskflow.exception.UnauthorizedAccessException;
import com.epam.taskflow.model.Board;
import com.epam.taskflow.model.ListEntity;
import com.epam.taskflow.model.User;
import com.epam.taskflow.repository.BoardRepository;
import com.epam.taskflow.repository.ListRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListService {

    private final ListRepository listRepository;
    private final BoardRepository boardRepository;
    private final BoardService boardService;
    private final ModelMapper modelMapper;
    private final PermissionService permissionService;

    @Transactional
    public ListDTO createList(ListDTO listDTO, Long userId) {
        Board board = boardRepository.findById(listDTO.getBoardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (!boardService.isUserAuthorized(board, userId)) {
            throw new UnauthorizedAccessException("User not authorized to add lists to this board");
        }
        permissionService.validateEditorAccess(board, userId);
        ListEntity list = modelMapper.map(listDTO, ListEntity.class);
        list.setBoard(board);

        if (list.getPosition() == null) {
            Integer maxPosition = listRepository.findMaxPositionByBoard(board);
            list.setPosition(maxPosition != null ? maxPosition + 1 : 0);
        }

        ListEntity savedList = listRepository.save(list);
        return modelMapper.map(savedList, ListDTO.class);
    }

    public List<ListDTO> getAllListsForBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (!boardService.isUserAuthorized(board, userId)) {
            throw new UnauthorizedAccessException("User not authorized to view lists of this board");
        }
        return listRepository.findByBoardOrderByPositionAsc(board).stream()
                .map(list -> modelMapper.map(list, ListDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public ListDTO updateList(Long listId, ListDTO listDTO, Long userId) {
        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));

        Board board = list.getBoard();
        if (!boardService.isUserAuthorized(board, userId)) {
            throw new UnauthorizedAccessException("User not authorized to update this list");
        }

        permissionService.validateEditorAccess(board, userId);

        list.setTitle(listDTO.getTitle());
        if (listDTO.getPosition() != null) {
            list.setPosition(listDTO.getPosition());
        }

        ListEntity updatedList = listRepository.save(list);
        return modelMapper.map(updatedList, ListDTO.class);
    }

    @Transactional
    public void deleteList(Long listId, Long userId) {
        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));

        Board board = list.getBoard();
        if (!boardService.isUserAuthorized(board, userId)) {
            throw new UnauthorizedAccessException("User not authorized to delete this list");
        }
        permissionService.validateEditorAccess(board, userId);

        listRepository.delete(list);
    }

    @Transactional
    public void moveList(Long listId, Integer newPosition, Long userId) {
        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));

        Board board = list.getBoard();
        if (!boardService.isUserAuthorized(board, userId)) {
            throw new UnauthorizedAccessException("User not authorized to move this list");
        }
        permissionService.validateEditorAccess(board, userId);


        List<ListEntity> lists = listRepository.findByBoardOrderByPositionAsc(board);

        lists.removeIf(l -> l.getId().equals(listId));

        if (newPosition < 0 || newPosition > lists.size()) {
            newPosition = lists.size();
        }

        lists.add(newPosition, list);

        for (int i = 0; i < lists.size(); i++) {
            lists.get(i).setPosition(i);
        }

        listRepository.saveAll(lists);
    }
}