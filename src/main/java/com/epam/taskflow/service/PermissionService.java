package com.epam.taskflow.service;

import com.epam.taskflow.model.Board;
import com.epam.taskflow.model.BoardUser;
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
    public void validateEditorAccess(Board board, Long userId) {
        if (board.getOwner() != null && board.getOwner().getId().equals(userId)) {
            return; // Owner always has editor access
        }

        Optional<BoardUser> boardUserOptional = boardUserRepository.findByUserIdAndBoard(userId, board);

        if (boardUserOptional.isPresent()) {
            BoardUser boardUser = boardUserOptional.get();
            if (boardUser.getRole() == BoardUser.Role.EDITOR) {
                return; // User has EDITOR role
            }
        }
        throw new UnauthorizedAccessException("User does not have editor access to this board.");
    }

}