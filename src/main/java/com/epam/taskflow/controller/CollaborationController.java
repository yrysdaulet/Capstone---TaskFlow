package com.epam.taskflow.controller;

import com.epam.taskflow.dto.CollaboratorDTO;
import com.epam.taskflow.model.User;
import com.epam.taskflow.service.BoardCollaborationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/collaborations")
@RequiredArgsConstructor
public class CollaborationController {

    private final BoardCollaborationService collaborationService;

    @PostMapping
    public ResponseEntity<Void> shareBoard(@RequestBody CollaboratorDTO collaboratorDTO,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long ownerId = ((User)userDetails).getId();
        collaborationService.shareBoard(collaboratorDTO, ownerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateCollaboratorRole(@RequestBody CollaboratorDTO collaboratorDTO,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        Long ownerId = ((User)userDetails).getId();
        collaborationService.updateCollaboratorRole(collaboratorDTO, ownerId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeCollaborator(@RequestBody CollaboratorDTO collaboratorDTO,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        Long ownerId = ((User)userDetails).getId();
        collaborationService.removeCollaborator(collaboratorDTO.getBoardId(), collaboratorDTO.getUserId(), ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<CollaboratorDTO>> getCollaborators(@PathVariable Long boardId,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User)userDetails).getId();
        List<CollaboratorDTO> collaborators = collaborationService.getCollaborators(boardId, userId);

        return ResponseEntity.ok(collaborators);
    }
}