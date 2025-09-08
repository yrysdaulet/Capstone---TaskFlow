package com.epam.taskflow.service;

import com.epam.taskflow.model.Board;
import com.epam.taskflow.model.ListEntity;
import com.epam.taskflow.repository.ListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ListServiceTest {

    @Mock private ListRepository listRepository;
    @Mock private BoardService boardService;
    @Mock private PermissionService permissionService;

    @InjectMocks
    private ListService listService;

    @Test
    void moveList_reindexes_and_bounds() {
        Board board = Board.builder().id(100L).build();
        ListEntity target = ListEntity.builder().id(1L).title("T").position(0).board(board).build();
        when(listRepository.findById(1L)).thenReturn(Optional.of(target));
        when(boardService.isUserAuthorized(board, 7L)).thenReturn(true);

        List<ListEntity> lists = new ArrayList<>();
        lists.add(ListEntity.builder().id(2L).position(0).board(board).build());
        lists.add(ListEntity.builder().id(3L).position(1).board(board).build());
        when(listRepository.findByBoardOrderByPositionAsc(board)).thenReturn(new ArrayList<>(lists));

        listService.moveList(1L, 10, 7L);

        ArgumentCaptor<List<ListEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(listRepository).saveAll(captor.capture());
        List<ListEntity> saved = captor.getValue();
        assertThat(saved).hasSize(3);
        assertThat(saved).extracting(ListEntity::getId).containsExactly(2L, 3L, 1L);
        assertThat(saved).extracting(ListEntity::getPosition).containsExactly(0, 1, 2);

        verify(permissionService).validateEditorAccess(board, 7L);
        verify(boardService).isUserAuthorized(board, 7L);
    }
}
