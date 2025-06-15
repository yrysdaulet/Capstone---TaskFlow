package com.epam.taskflow.dto;

import com.epam.taskflow.model.BoardUser;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaboratorDTO {
    private Long userId;
    private Long boardId;
    private BoardUser.Role role;
}