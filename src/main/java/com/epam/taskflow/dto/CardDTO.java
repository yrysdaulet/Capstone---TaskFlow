package com.epam.taskflow.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDTO {
    private Long id;
    @NotBlank
    private String title;
    private String description;
    @FutureOrPresent(message = "Due date must be in the future or present")
    private LocalDateTime dueDate;
    private Integer position;
    private Long listId;
}