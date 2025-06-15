package com.epam.taskflow.service;

import com.epam.taskflow.dto.CollaboratorDTO;
import com.epam.taskflow.exception.ResourceAlreadyExistsException;
import com.epam.taskflow.exception.ResourceNotFoundException;
import com.epam.taskflow.exception.UnauthorizedAccessException;
import com.epam.taskflow.model.Board;
import com.epam.taskflow.model.BoardUser;
import com.epam.taskflow.model.User;
import com.epam.taskflow.repository.BoardUserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardCollaborationService {

    private final BoardUserRepository boardUserRepository;
    private final BoardService boardService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Transactional
    public void shareBoard(CollaboratorDTO collaboratorDTO, Long ownerId) {
        Board board = boardService.getBoardEntity(collaboratorDTO.getBoardId());

        // Only owner can share the board
        if (!board.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedAccessException("Only the board owner can share the board");
        }

        User userToShareWith = userService.getUserEntity(collaboratorDTO.getUserId());

        // Can't share with yourself
        if (userToShareWith.getId().equals(ownerId)) {
            throw new UnauthorizedAccessException("Cannot share board with yourself");
        }

        // Check if already shared
        if (boardUserRepository.existsByUserAndBoard(userToShareWith, board)) {
            throw new ResourceAlreadyExistsException("Board already shared with this user");
        }

        BoardUser boardUser = BoardUser.builder()
                .user(userToShareWith)
                .board(board)
                .role(collaboratorDTO.getRole())
                .build();

        boardUserRepository.save(boardUser);
    }

    @Transactional
    public void updateCollaboratorRole(CollaboratorDTO collaboratorDTO, Long ownerId) {
        Board board = boardService.getBoardEntity(collaboratorDTO.getBoardId());

        // Only owner can update roles
        if (!board.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedAccessException("Only the board owner can update collaborator roles");
        }

        User collaborator = userService.getUserEntity(collaboratorDTO.getUserId());

        BoardUser boardUser = boardUserRepository.findByUserAndBoard(collaborator, board)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found"));

        boardUser.setRole(collaboratorDTO.getRole());
        boardUserRepository.save(boardUser);
    }

    @Transactional
    public void removeCollaborator(Long boardId, Long userId, Long ownerId) {
        Board board = boardService.getBoardEntity(boardId);

        // Only owner can remove collaborators
        if (!board.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedAccessException("Only the board owner can remove collaborators");
        }

        User collaborator = userService.getUserEntity(userId);

        BoardUser boardUser = boardUserRepository.findByUserAndBoard(collaborator, board)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found"));

        boardUserRepository.delete(boardUser);
    }

    public List<CollaboratorDTO> getCollaborators(Long boardId, Long userId) {
        Board board = boardService.getBoardEntity(boardId);

        // Only owner or collaborators can see the list
        if (!board.getOwner().getId().equals(userId) &&
                !boardUserRepository.existsByUserAndBoard(userService.getUserEntity(userId), board)) {
            throw new UnauthorizedAccessException("Not authorized to view collaborators");
        }

        return boardUserRepository.findByBoard(board).stream()
                .map(user->modelMapper.map(user, CollaboratorDTO.class))
                .collect(Collectors.toList());
    }
    public void validateEditorAccess(Board board, User user) {
        if (isOwner(board, user)) return;

        BoardUser.Role role = boardUserRepository.findByUserAndBoard(user, board)
                .orElseThrow(() -> new UnauthorizedAccessException("Not a collaborator"))
                .getRole();

        if (role != BoardUser.Role.EDITOR) {
            throw new UnauthorizedAccessException("Editor role required");
        }
    }

    private boolean isOwner(Board board, User user) {
        return board.getOwner().equals(user);
    }

}