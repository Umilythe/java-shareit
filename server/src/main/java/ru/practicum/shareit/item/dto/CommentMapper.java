package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

public class CommentMapper {
    public static CommentDtoResponse toCommentDto(Comment comment) {
        return CommentDtoResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .item(comment.getItem())
                .created(comment.getCreated())
                .build();
    }

    public static Comment toComment(CommentDtoRequest commentDtoRequest, User author, Item item, LocalDateTime time) {
        return Comment.builder()
                .text(commentDtoRequest.getText())
                .author(author)
                .item(item)
                .created(time)
                .build();
    }
}
