package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long ownerId);

    ItemDtoWithBooking getById(Long userId, Long id);

    ItemDto update(ItemDto itemDto, Long ownerId, Long itemId);

    List<ItemDtoWithBooking> getByOwner(Long ownerId);

    List<ItemDto> search(String text);

    CommentDtoResponse addComment(CommentDtoRequest commentDtoRequest, Long itemId, Long userId);
}