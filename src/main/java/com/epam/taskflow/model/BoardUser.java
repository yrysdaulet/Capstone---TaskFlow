package com.epam.taskflow.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "board_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(BoardUserKey.class)
public class BoardUser {
    public enum Role {
        VIEWER, EDITOR
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}

