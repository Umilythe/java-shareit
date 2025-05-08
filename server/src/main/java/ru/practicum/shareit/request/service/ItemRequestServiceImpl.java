package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestStorage itemRequestStorage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Override
    @Transactional
    public ItemRequestDtoResponse create(ItemRequestDto itemRequestDto, Long userId) {
        User requestor = findUser(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, requestor);
        return ItemRequestMapper.toItemRequestDto(itemRequestStorage.save(itemRequest));
    }

    @Override
    public List<ItemRequestDtoResponse> findUserRequests(Long userId) {
        findUser(userId);
     List<ItemRequestDtoResponse> itemRequestDtoResponses = itemRequestStorage.findAllByRequestorIdOrderByCreatedDesc(userId)
             .stream()
             .map(ItemRequestMapper::toItemRequestDto)
             .collect(Collectors.toList());
        itemRequestDtoResponses.forEach(itemRequestDto -> itemRequestDto.setItems(itemStorage.findAllByRequestId(itemRequestDto.getId())
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList())));
        return itemRequestDtoResponses;
    }

    @Override
    public List<ItemRequestDtoResponse> findAllRequests(Long userId) {
      return itemRequestStorage.findAllByRequestorIdNotOrderByCreatedDesc(userId)
              .stream()
              .map(ItemRequestMapper::toItemRequestDto)
              .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDtoResponse findRequestById(Long requestId, Long userId) {
        findUser(userId);
        ItemRequest itemRequest = itemRequestStorage.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + requestId + " не найден."));
        ItemRequestDtoResponse itemRequestDtoResponse = ItemRequestMapper.toItemRequestDto(itemRequest);
        List<ItemDto> itemDtos = itemStorage.findAllByRequestId(requestId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        itemRequestDtoResponse.setItems(itemDtos);
        return itemRequestDtoResponse;
    }

    private User findUser(Long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> (new NotFoundException("Пользователь не найден.")));
        return user;
    }
}
