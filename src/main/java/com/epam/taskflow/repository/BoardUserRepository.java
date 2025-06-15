package com.epam.taskflow.repository;

import com.epam.taskflow.model.Board;
import com.epam.taskflow.model.BoardUser;
import com.epam.taskflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardUserRepository extends JpaRepository<BoardUser, Long> {
    Optional<BoardUser> findByUserAndBoard(User user, Board board);
    List<BoardUser> findByUser(User user);
    List<BoardUser> findByBoard(Board board);
    boolean existsByUserAndBoard(User user, Board board);

    Optional<BoardUser> findByUserIdAndBoard(Long userId, Board board);
}