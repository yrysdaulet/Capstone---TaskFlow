package com.epam.taskflow.repository;

import com.epam.taskflow.model.Board;
import com.epam.taskflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByOwner(User owner);
}