package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exceptions.EmptyInformationException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserService userService;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;

    @Override
    @Transactional
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
        return ItemMapper.toItemDto(itemStorage.save(item));
    }

    @Override
    @Transactional
    public ItemDtoWithBooking getById(Long userId, Long id) {
        Item item = getItem(id);
        List<CommentDtoResponse> comments = commentStorage.findAllByItemId(id).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        return ItemMapper.toItemDtoWithBooking(item, null, null, comments);
    }

    @Override
    @Transactional
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
        return ItemMapper.toItemDto(itemStorage.save(item));
    }

    @Override
    @Transactional
    public List<ItemDtoWithBooking> getByOwner(Long ownerId) {
        userService.getById(ownerId);
        List<ItemDtoWithBooking> itemsForResponse = new ArrayList<>();
        List<Item> items = itemStorage.findByOwnerId(ownerId);
        for (Item item : items) {
            List<CommentDtoResponse> comments = commentStorage.findAllByItemId(item.getId()).stream()
                    .map(CommentMapper::toCommentDto)
                    .toList();
            Booking nextBooking = bookingStorage.findTop1ByItemIdAndStartAfterAndStatusOrderByStartAsc(item.getId(), LocalDateTime.now(), Status.APPROVED);
            Booking lastBooking = bookingStorage.findTop1ByItemIdAndEndBeforeAndStatusOrderByEndDesc(item.getId(), LocalDateTime.now(), Status.APPROVED);
            ItemDtoWithBooking itemToList = ItemMapper.toItemDtoWithBooking(item, nextBooking, lastBooking, comments);
            itemsForResponse.add(itemToList);
        }
        return itemsForResponse;
    }

    @Override
    @Transactional
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return itemStorage.search(text).stream()
                .filter(Item::isAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDtoResponse addComment(CommentDtoRequest commentDtoRequest, Long itemId, Long userId) {
        UserDto authorDto = userService.getById(userId);
        Item item = getItem(itemId);
        List<Booking> bookings = bookingStorage.findAllByBookerIdAndItemIdAndEndBefore(userId, itemId, LocalDateTime.now());
        if (bookings.isEmpty()) {
            throw new ValidationException("Пользователь с id =  " + userId + " никогда не арендовал вещь с id = " + itemId);
        }
        Comment comment = commentStorage.save(CommentMapper.toComment(commentDtoRequest, UserMapper.toUser(authorDto), item, LocalDateTime.now()));
        return CommentMapper.toCommentDto(comment);
    }

    private Item getItem(Long id) {
        return itemStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + id + " не найдена"));
    }
}
