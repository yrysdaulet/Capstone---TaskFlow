package com.epam.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDTO {
    private Long id;
    @NotBlank
    private String title;
    private LocalDateTime createdAt;
    private Long ownerId;
}