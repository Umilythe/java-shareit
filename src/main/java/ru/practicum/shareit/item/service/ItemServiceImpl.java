package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EmptyInformationException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserService userService;

    @Override
    public ItemDto create(ItemDto itemDto, Long ownerId) {
        if (itemDto.getName() == null || itemDto.getName().isEmpty()) {
            throw new EmptyInformationException("Имя не может быть пустым.");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isEmpty()) {
            throw new EmptyInformationException("Описание не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new EmptyInformationException("Статус не может быть пустым");
        }
        User owner = UserMapper.toUser(userService.getById(ownerId));
        Item item = ItemMapper.toItem(itemDto, owner, null);
        return ItemMapper.toItemDto(itemStorage.create(item));
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = getItem(id);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long ownerId, Long itemId) {
        Item item = getItem(itemId);
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Изменять товар может только его владелец.");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemStorage.update(item, itemId));
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        List<Item> items = itemStorage.getByOwner(ownerId);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return itemStorage.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private Item getItem(Long id) {
        return itemStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + id + " не найдена"));
    }
}
