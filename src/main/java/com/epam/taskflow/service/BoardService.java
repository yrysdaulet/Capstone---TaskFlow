package com.epam.taskflow.service;

import com.epam.taskflow.dto.BoardDTO;
import com.epam.taskflow.exception.ResourceNotFoundException;
import com.epam.taskflow.exception.UnauthorizedAccessException;
import com.epam.taskflow.model.Board;
import com.epam.taskflow.model.BoardUser;
import com.epam.taskflow.model.User;
import com.epam.taskflow.repository.BoardRepository;
import com.epam.taskflow.repository.BoardUserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserService userService;
    private final BoardUserRepository boardUserRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public BoardDTO createBoard(BoardDTO boardDTO, Long userId) {
        User owner = userService.getUserEntity(userId);
        Board board = modelMapper.map(boardDTO, Board.class);
        board.setOwner(owner);
        Board savedBoard = boardRepository.save(board);
        return modelMapper.map(savedBoard, BoardDTO.class);
    }

    public List<BoardDTO> getAllBoardsForUser(Long userId) {
        User user = userService.getUserEntity(userId);
        List<Board> ownedBoards = boardRepository.findByOwner(user);
        List<Board> sharedBoards = boardUserRepository.findByUser(user).stream()
                .map(BoardUser::getBoard)
                .collect(Collectors.toList());

        ownedBoards.addAll(sharedBoards);

        return ownedBoards.stream()
                .distinct()
                .map(board -> modelMapper.map(board, BoardDTO.class))
                .collect(Collectors.toList());
    }

    public BoardDTO getBoardById(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (!isUserAuthorized(board, userId)) {
            throw new UnauthorizedAccessException("User not authorized to access this board");
        }

        return modelMapper.map(board, BoardDTO.class);
    }

    @Transactional
    public BoardDTO updateBoard(Long boardId, BoardDTO boardDTO, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (!board.getOwner().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Only the owner can update the board");
        }

        board.setTitle(boardDTO.getTitle());
        Board updatedBoard = boardRepository.save(board);
        return modelMapper.map(updatedBoard, BoardDTO.class);
    }

    @Transactional
    public void deleteBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (!board.getOwner().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Only the owner can delete the board");
        }

        boardRepository.delete(board);
    }
    @Transactional
    boolean isUserAuthorized(Board board, Long userId) {
        return board.getOwner().getId().equals(userId) ||
                boardUserRepository.existsByUserAndBoard(
                        userService.getUserEntity(userId), board);
    }
    @Transactional
    public Board getBoardEntity(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
    }
}