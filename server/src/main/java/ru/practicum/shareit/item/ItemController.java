package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public Collection<ItemDtoWithBooking> returnItemsOfUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на предоставление данных о списке вещей пользователя с id = " + userId);
        return itemService.getByOwner(userId);
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto item) {
        log.info("Получен запрос на создание вещи");
        return itemService.create(item, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId, @RequestBody ItemDto newItem) {
        log.info("Получен запрос на обновление информации о вещи с id = " + itemId);
        return itemService.update(newItem, userId, itemId);
    }

    @GetMapping("{itemId}")
    public ItemDtoWithBooking getItemById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        log.info("Получен запрос на предоставление данных о вещи id = " + itemId);
        return itemService.getById(userId, itemId);
    }

    @GetMapping("search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("Получен запрос на предоставление данных о вещи по ключевым словам");
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoResponse addComment(@RequestBody CommentDtoRequest dto,
                                         @PathVariable Long itemId,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на публикацию комментария");
        return itemService.addComment(dto, itemId, userId);
    }

}