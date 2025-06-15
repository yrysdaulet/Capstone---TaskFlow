package com.epam.taskflow.repository;

import com.epam.taskflow.model.Board;
import com.epam.taskflow.model.Card;
import com.epam.taskflow.model.ListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByListOrderByPositionAsc(ListEntity list);

    @Query("select max(c.position) from Card c where c.list = :list")
    Integer findMaxPositionByList(@Param("list") ListEntity list);

}