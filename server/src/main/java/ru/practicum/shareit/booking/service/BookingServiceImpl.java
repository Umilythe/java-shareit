package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.Status.*;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingStorage bookingStorage;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public BookingDto create(BookingDtoRequest bookingDtoRequest, Long userId) {
        Item item = itemStorage.findById(bookingDtoRequest.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + " не найдена."));
        User booker = findUser(userId);
        if (!item.isAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования!");
        }
        if (booker.getId().equals(item.getOwner().getId())) {
            throw new ValidationException("Пользователь не может забронировать свою вещь.");
        }
        Booking booking = BookingMapper.toBooking(bookingDtoRequest, item);
        booking.setBooker(booker);
        booking.setStatus(WAITING);
        return BookingMapper.toBookingDto(bookingStorage.save(booking));
    }

    @Override
    public BookingDto changeBookingStatus(Long userId, Long bookingId, Boolean approved) {
        Booking booking = findBooking(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Только владелец вещи может изменить статус.");
        }
        if (!booking.getStatus().equals(WAITING)) {
            throw new ValidationException("Только статус WAITING может быть изменен.");
        }
        if (approved) {
            booking.setStatus(APPROVED);
        } else {
            booking.setStatus(REJECTED);
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        Booking booking = findBooking(bookingId);
        findUser(userId);
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Информацию о бронировании может получить только владелец вещи или арендатор.");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookingsByState(Long userId, String state) {
        findUser(userId);
        List<Booking> bookings;
        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingStorage.findAllByBookerIdOrderByStartDesc(userId);
                break;
            case "CURRENT":
                bookings = bookingStorage.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
                break;
            case "PAST":
                bookings = bookingStorage.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case "FUTURE":
                bookings = bookingStorage.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case "WAITING":
                bookings = bookingStorage.findAllByBookerIdAndStatusOrderByStartDesc(userId, WAITING);
                break;
            case "REJECTED":
                bookings = bookingStorage.findAllByBookerIdAndStatusOrderByStartDesc(userId, REJECTED);
                break;
            default:
                throw new ValidationException("State не распознано.");
        }
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getUserItemsBookingsByState(Long userId, String state) {
        findUser(userId);
        List<Booking> bookings;
        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingStorage.findAllByItemOwnerIdOrderByStartDesc(userId);
                break;
            case "CURRENT":
                bookings = bookingStorage.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
                break;
            case "PAST":
                bookings = bookingStorage.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case "FUTURE":
                bookings = bookingStorage.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case "WAITING":
                bookings = bookingStorage.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, WAITING);
                break;
            case "REJECTED":
                bookings = bookingStorage.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, REJECTED);
                break;
            default:
                throw new ValidationException("State не распознано.");
        }
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private User findUser(Long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
        return user;
    }

    private Booking findBooking(Long bookingId) {
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id = " + bookingId + " не найдено."));
        return booking;
    }
}
