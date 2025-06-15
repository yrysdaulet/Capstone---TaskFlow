package com.epam.taskflow.repository;

import com.epam.taskflow.model.Board;
import com.epam.taskflow.model.ListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListRepository extends JpaRepository<ListEntity, Long> {
    Optional<ListEntity> findByIdAndBoard(Long id, Board board);
    List<ListEntity> findByBoardOrderByPositionAsc(Board board);


    @Query("select max(l.position) from ListEntity l where l.board = :board")
    Integer findMaxPositionByBoard(@Param("board") Board board);
}