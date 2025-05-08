package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingDtoRequest bookingDtoRequest, Long userId);

    BookingDto changeBookingStatus(Long userId, Long bookingId, Boolean approved);

    BookingDto getBooking(Long userId, Long bookingId);

    List<BookingDto> getUserBookingsByState(Long userId, String state);

    List<BookingDto> getUserItemsBookingsByState(Long userId, String state);
}
