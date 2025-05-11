package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.EmptyInformationException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> returnItemsOfUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.getByOwner(userId);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody @Valid ItemDto item) {
        if (item.getName() == null || item.getName().isEmpty()) {
            throw new EmptyInformationException("Имя не может быть пустым.");
        }
        if (item.getDescription() == null || item.getDescription().isEmpty()) {
            throw new EmptyInformationException("Описание не может быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new EmptyInformationException("Статус не может быть пустым");
        }
        return itemClient.create(item, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId, @RequestBody @Valid ItemDto newItem) {
        return itemClient.update(newItem, userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        return itemClient.getById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text) {
        if (text == null || text.isBlank()) {
            return ResponseEntity.noContent().build();
        }
        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestBody CommentDtoRequest dto,
                                         @PathVariable Long itemId,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        if (dto.getText() == null || dto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым.");
        }
        return itemClient.addComment(dto, itemId, userId);
    }

}