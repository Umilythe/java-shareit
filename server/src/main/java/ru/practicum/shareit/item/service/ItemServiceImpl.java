package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exceptions.EmptyInformationException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserService userService;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;
    private final ItemRequestStorage itemRequestStorage;

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
        ItemRequest itemRequest = null;
        if (itemDto.getRequestId() != null) {
            itemRequest = itemRequestStorage.findById(itemDto.getRequestId()).orElse(null);
        }
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
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
        List<Item> items = itemStorage.findByOwnerId(ownerId);
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        List<ItemDtoWithBooking> itemsForResponse = new ArrayList<>();
        List<Booking> bookings = bookingStorage.findAllByItemIdOrderByStartAsc(itemIds);
        Map<Long, List<Booking>> bookingMap = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));
        List<Comment> comments = commentStorage.findAllByItemIdIn(itemIds);
        Map<Long, List<CommentDtoResponse>> commentsMap = comments.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())));
        for (Item item : items) {
            List<CommentDtoResponse> itemComments = commentsMap.get(item.getId());
            List<Booking> itemBookings;
            if (bookingMap.get(item.getId()) == null) {
                itemBookings = Collections.emptyList();
            } else {
                itemBookings = bookingMap.get(item.getId());
            }
            Booking nextBooking = itemBookings.stream()
                    .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                    .min(Comparator.comparing(Booking::getStart))
                    .orElse(null);
            Booking lastBooking = itemBookings.stream()
                    .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                    .max(Comparator.comparing(Booking::getEnd))
                    .orElse(null);
            ItemDtoWithBooking itemToList = ItemMapper.toItemDtoWithBooking(item, nextBooking, lastBooking, itemComments);
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
        if (commentDtoRequest.getText() == null || commentDtoRequest.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым.");
        }
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
