package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDtoResponse {
    private Long id;

    private String text;

    private Item item;

    private String authorName;

    private LocalDateTime created;
}
