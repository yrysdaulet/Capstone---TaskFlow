package com.epam.taskflow.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDTO {
    private Long id;
    private String title;
    private Integer position;
    private Long boardId;
}