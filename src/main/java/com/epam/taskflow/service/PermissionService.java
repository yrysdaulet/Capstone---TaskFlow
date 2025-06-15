package com.epam.taskflow.service;

import com.epam.taskflow.model.Board;
import com.epam.taskflow.model.BoardUser;
import com.epam.taskflow.model.User;
import com.epam.taskflow.exception.UnauthorizedAccessException;
import com.epam.taskflow.repository.BoardUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PermissionService {

    private final BoardUserRepository boardUserRepository;

    public PermissionService(BoardUserRepository boardUserRepository) {
        this.boardUserRepository = boardUserRepository;
    }
    @Transactional
    public void validateEditorAccess(Board board, User user) {
        if (board.getOwner() != null && board.getOwner().getId().equals(user.getId())) {
            return; // Owner always has editor access
        }

        Optional<BoardUser> boardUserOptional = boardUserRepository.findByUserAndBoard(user, board);

        if (boardUserOptional.isPresent()) {
            BoardUser boardUser = boardUserOptional.get();
            if (boardUser.getRole() == BoardUser.Role.EDITOR) {
                return; // User has EDITOR role
            }
        }
        throw new UnauthorizedAccessException("User does not have editor access to this board.");
    }

    @Transactional
    public void validateViewerAccess(Board board, User user) {
        if (board.getOwner() != null && board.getOwner().getId().equals(user.getId())) {
            return; // Owner always has access
        }

        Optional<BoardUser> boardUserOptional = boardUserRepository.findByUserAndBoard(user, board);

        if (boardUserOptional.isPresent()) {
            BoardUser boardUser = boardUserOptional.get();
            if (boardUser.getRole() == BoardUser.Role.EDITOR || boardUser.getRole() == BoardUser.Role.VIEWER) {
                return; // User has at least VIEWER role
            }
        }
        throw new UnauthorizedAccessException("User does not have access to view this board.");
    }

}