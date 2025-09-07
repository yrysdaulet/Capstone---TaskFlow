package com.epam.taskflow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDTO {
    private Long id;
    @NotBlank
    private String title;
    @Min(0)
    private Integer position;
    private Long boardId;
}