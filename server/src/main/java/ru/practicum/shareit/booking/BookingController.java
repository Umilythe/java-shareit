package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestBody BookingDtoRequest bookingDtoRequest,
                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на создание бронирования.");
        return bookingService.create(bookingDtoRequest, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto changeBookingStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long bookingId,
                                          @RequestParam Boolean approved) {
        log.info("Получен запрос на обновление статуса бронирования");
        return bookingService.changeBookingStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long bookingId) {
        log.info("Получен запрос на предоставление данных о бронировании с id = " + bookingId);
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> findUserBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestParam(name = "state", defaultValue = "ALL") String state) {
        log.info("Получен запрос на предоставление данных о бронированиях пользователя с id = " + userId);
        return bookingService.getUserBookingsByState(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> findBookingByUserItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(name = "state", defaultValue = "ALL") String state) {
        log.info("Получен запрос на предоставление данных о бронированиях вещей пользователя с с id = " + userId);
        return bookingService.getUserItemsBookingsByState(userId, state);
    }
}
