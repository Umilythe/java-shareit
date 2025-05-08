package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDtoResponse create(ItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestDtoResponse> findUserRequests(Long userId);

    List<ItemRequestDtoResponse> findAllRequests(Long userId);

    ItemRequestDtoResponse findRequestById(Long requestId, Long userId);
}
