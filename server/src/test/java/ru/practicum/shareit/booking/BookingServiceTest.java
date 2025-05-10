package ru.practicum.shareit.booking;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.model.Status.APPROVED;
import static ru.practicum.shareit.booking.model.Status.REJECTED;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class BookingServiceTest {

    @Autowired
    ItemServiceImpl itemService;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    BookingServiceImpl bookingService;

    static UserDto userDtoRequest1;
    static UserDto userDtoRequest2;

    static ItemDto itemDtoRequest1;
    static ItemDto itemDtoRequest2;


    @BeforeAll
    static void init() {
        userDtoRequest1 = new UserDto(null, "user1", "user1@test.com");
        userDtoRequest2 = new UserDto(null, "user2", "user2@test.com");

        itemDtoRequest1 = new ItemDto(null, "test1", "description1", true, null, null);
        itemDtoRequest2 = new ItemDto(null, "test2", "description2", true, null, null);
    }

    @Test
    void shouldCreateBooking() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());
        BookingDtoRequest bookingDtoRequest = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(30));
        BookingDto bookingDtoResponse = bookingService.create(bookingDtoRequest, userDtoResponse2.getId());

        Assertions.assertThat(bookingDtoResponse.getItem().getId()).isEqualTo(bookingDtoRequest.getItemId());
        Assertions.assertThat(bookingDtoResponse.getBooker().getId()).isEqualTo(userDtoResponse2.getId());
        Assertions.assertThat(bookingDtoResponse.getStart()).isEqualTo(bookingDtoRequest.getStart());
        Assertions.assertThat(bookingDtoResponse.getEnd()).isEqualTo(bookingDtoRequest.getEnd());
    }

    @Test
    void shouldNotCreateBookingNoUser() {
        UserDto userDtoResponse = userService.create(userDtoRequest1);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse.getId());
        BookingDtoRequest bookingDtoRequest = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().minusHours(1), LocalDateTime.now().minusMinutes(30));

        Assertions.assertThatThrownBy(() ->
                bookingService.create(bookingDtoRequest, 2L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldNotCreateBookingForOwner() {
        UserDto userDtoResponse = userService.create(userDtoRequest1);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse.getId());
        BookingDtoRequest bookingDtoRequest = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().minusHours(1), LocalDateTime.now().minusMinutes(30));

        Assertions.assertThatThrownBy(() ->
                bookingService.create(bookingDtoRequest, userDtoResponse.getId())).isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldNotCreateBookingWithEndBeforePast() {
        UserDto userDtoResponse = userService.create(userDtoRequest1);
        UserDto booker = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse.getId());
        BookingDtoRequest bookingDtoRequest = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(1));

        Assertions.assertThatThrownBy(() ->
                bookingService.create(bookingDtoRequest, booker.getId())).isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldNotCreateBookingNotAvailableItem() {
        UserDto userDtoResponse = userService.create(userDtoRequest1);
        ItemDto itemDtoNotAvailable =
                new ItemDto(null, "test1", "description1", false, null, null);
        ItemDto itemDtoResponse = itemService.create(itemDtoNotAvailable, userDtoResponse.getId());
        BookingDtoRequest bookingDtoRequest = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(30));

        Assertions.assertThatThrownBy(() ->
                        bookingService.create(bookingDtoRequest, userDtoResponse.getId()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldApproveBooking() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());
        BookingDtoRequest bookingDtoRequest = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(30));
        BookingDto bookingDtoResponse = bookingService.create(bookingDtoRequest, userDtoResponse2.getId());

        BookingDto bookingApproveDto = bookingService.changeBookingStatus(userDtoResponse1.getId(), bookingDtoResponse.getId(),
                true);

        Assertions.assertThat(bookingApproveDto.getStatus()).isEqualTo(APPROVED);
    }

    @Test
    void shouldNotApproveBooking() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());
        BookingDtoRequest bookingDtoRequest = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(30));
        BookingDto bookingDtoResponse = bookingService.create(bookingDtoRequest, userDtoResponse2.getId());

        BookingDto bookingApproveDto = bookingService.changeBookingStatus(userDtoResponse1.getId(), bookingDtoResponse.getId(),
                false);

        Assertions.assertThat(bookingApproveDto.getStatus()).isEqualTo(REJECTED);
    }

    @Test
    void update_shouldNotApproveNoBooking() {
        UserDto userDtoResponse = userService.create(userDtoRequest1);

        Assertions.assertThatThrownBy(() ->
                        bookingService.changeBookingStatus(userDtoResponse.getId(), 1L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFindBookingById() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());
        BookingDtoRequest bookingDtoRequest = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(30));
        BookingDto bookingDtoResponse = bookingService.create(bookingDtoRequest, userDtoResponse2.getId());

        BookingDto findBookingDto = bookingService.getBooking(userDtoResponse2.getId(), bookingDtoResponse.getId());

        Assertions.assertThat(bookingDtoResponse.getItem().getId()).isEqualTo(findBookingDto.getItem().getId());
        Assertions.assertThat(bookingDtoResponse.getStart()).isEqualTo(findBookingDto.getStart());
        Assertions.assertThat(bookingDtoResponse.getEnd()).isEqualTo(findBookingDto.getEnd());
    }

    @Test
    void findById_shouldNotFindBookingByIdNoBooking() {
        UserDto userDtoResponse = userService.create(userDtoRequest1);

        Assertions.assertThatThrownBy(() ->
                bookingService.getBooking(userDtoResponse.getId(), 2L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFindAllBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusMinutes(2), LocalDateTime.now().plusHours(115));
        BookingDtoRequest bookingDtoRequest2 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusHours(65));
        BookingDtoRequest bookingDtoRequest3 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusMinutes(3), LocalDateTime.now().plusHours(185));

        bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest2, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest3, userDtoResponse2.getId());

        List<BookingDto> bookings = bookingService.getUserBookingsByState(userDtoResponse2.getId(), "ALL");

        Assertions.assertThat(bookings.size()).isEqualTo(3);
    }

    @Test
    void getUserItemsBookingsByState_shouldNotFindNoUser() {
        Assertions.assertThatThrownBy(() ->
                bookingService.getUserItemsBookingsByState(1L, "ALL")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFindFutureBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
        BookingDtoRequest bookingDtoRequest2 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(7));
        BookingDtoRequest bookingDtoRequest3 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(8), LocalDateTime.now().plusHours(15));

        bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest2, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest3, userDtoResponse2.getId());

        List<BookingDto> bookings = bookingService.getUserBookingsByState(userDtoResponse2.getId(), "FUTURE");

        Assertions.assertThat(bookings.size()).isEqualTo(3);
    }

    @Test
    void shouldFindPastBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().minusHours(10), LocalDateTime.now().minusHours(5));
        BookingDtoRequest bookingDtoRequest2 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().minusHours(4), LocalDateTime.now().minusHours(2));
        BookingDtoRequest bookingDtoRequest3 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusMinutes(3), LocalDateTime.now().plusHours(185));

        bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest2, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest3, userDtoResponse2.getId());

        List<BookingDto> bookings = bookingService.getUserBookingsByState(userDtoResponse2.getId(), "PAST");

        Assertions.assertThat(bookings.size()).isEqualTo(2);
    }

    @Test
    void shouldFindCurrentBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().minusHours(10), LocalDateTime.now().plusHours(5));
        BookingDtoRequest bookingDtoRequest2 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().minusHours(4), LocalDateTime.now().plusHours(2));
        BookingDtoRequest bookingDtoRequest3 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusMinutes(3), LocalDateTime.now().plusHours(185));

        bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest2, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest3, userDtoResponse2.getId());

        List<BookingDto> bookings = bookingService.getUserBookingsByState(userDtoResponse2.getId(), "CURRENT");

        Assertions.assertThat(bookings.size()).isEqualTo(2);
    }

    @Test
    void shouldFindWaitingBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
        BookingDtoRequest bookingDtoRequest2 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(7));
        BookingDtoRequest bookingDtoRequest3 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(8), LocalDateTime.now().plusHours(15));

        bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest2, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest3, userDtoResponse2.getId());

        List<BookingDto> bookings = bookingService.getUserBookingsByState(userDtoResponse2.getId(), "WAITING");

        Assertions.assertThat(bookings.size()).isEqualTo(3);
    }

    @Test
    void shouldFindAllItemBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
        BookingDtoRequest bookingDtoRequest2 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(7));
        BookingDtoRequest bookingDtoRequest3 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(8), LocalDateTime.now().plusHours(15));


        bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest2, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest3, userDtoResponse2.getId());

        List<BookingDto> bookings = bookingService.getUserItemsBookingsByState(userDtoResponse1.getId(), "All");

        Assertions.assertThat(bookings.size()).isEqualTo(3);
    }

    @Test
    void shouldFindPastItemBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().minusHours(5), LocalDateTime.now().minusHours(2));
        BookingDtoRequest bookingDtoRequest2 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(1));
        BookingDtoRequest bookingDtoRequest3 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(8), LocalDateTime.now().plusHours(15));


        bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest2, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest3, userDtoResponse2.getId());

        List<BookingDto> bookings = bookingService.getUserItemsBookingsByState(userDtoResponse1.getId(), "PAST");

        Assertions.assertThat(bookings.size()).isEqualTo(2);
    }

    @Test
    void shouldFindWaitingItemBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
        BookingDtoRequest bookingDtoRequest2 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(7));
        BookingDtoRequest bookingDtoRequest3 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(8), LocalDateTime.now().plusHours(15));


        bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest2, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest3, userDtoResponse2.getId());

        List<BookingDto> bookings = bookingService.getUserItemsBookingsByState(userDtoResponse1.getId(), "WAITING");

        Assertions.assertThat(bookings.size()).isEqualTo(3);
    }

    @Test
    void shouldFindCurrentItemBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(2));
        BookingDtoRequest bookingDtoRequest2 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().minusHours(4), LocalDateTime.now().plusHours(7));
        BookingDtoRequest bookingDtoRequest3 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(8), LocalDateTime.now().plusHours(15));


        bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest2, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest3, userDtoResponse2.getId());

        List<BookingDto> bookings = bookingService.getUserItemsBookingsByState(userDtoResponse1.getId(), "CURRENT");

        Assertions.assertThat(bookings.size()).isEqualTo(2);
    }

    @Test
    void shouldFindFutureItemBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
        BookingDtoRequest bookingDtoRequest2 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(7));
        BookingDtoRequest bookingDtoRequest3 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(8), LocalDateTime.now().plusHours(15));


        bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest2, userDtoResponse2.getId());
        bookingService.create(bookingDtoRequest3, userDtoResponse2.getId());

        List<BookingDto> bookings = bookingService.getUserItemsBookingsByState(userDtoResponse1.getId(), "FUTURE");

        Assertions.assertThat(bookings.size()).isEqualTo(3);
    }

    @Test
    void shouldNotFindItemBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());

        Assertions.assertThatThrownBy(() -> bookingService.getUserItemsBookingsByState(userDtoResponse1.getId(), "STATE")).isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldNotFindBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusMinutes(2), LocalDateTime.now().plusHours(115));

        bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());

        Assertions.assertThatThrownBy(() -> bookingService.getUserBookingsByState(userDtoResponse2.getId(), "STATE")).isInstanceOf(ValidationException.class);
    }


    @Test
    void shouldFindRejectedBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
        BookingDtoRequest bookingDtoRequest2 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(7));
        BookingDtoRequest bookingDtoRequest3 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(8), LocalDateTime.now().plusHours(15));

        BookingDto bookingDto1 = bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());
        BookingDto bookingDto2 = bookingService.create(bookingDtoRequest2, userDtoResponse2.getId());
        BookingDto bookingDto3 = bookingService.create(bookingDtoRequest3, userDtoResponse2.getId());

        bookingService.changeBookingStatus(userDtoResponse1.getId(), bookingDto1.getId(), false);
        bookingService.changeBookingStatus(userDtoResponse1.getId(), bookingDto2.getId(), false);
        bookingService.changeBookingStatus(userDtoResponse1.getId(), bookingDto3.getId(), false);

        List<BookingDto> bookings = bookingService.getUserBookingsByState(userDtoResponse2.getId(), "REJECTED");

        Assertions.assertThat(bookings.size()).isEqualTo(3);
    }

    @Test
    void shouldFindRejectedItemBookings() {
        UserDto userDtoResponse1 = userService.create(userDtoRequest1);
        UserDto userDtoResponse2 = userService.create(userDtoRequest2);
        ItemDto itemDtoResponse = itemService.create(itemDtoRequest1, userDtoResponse1.getId());

        BookingDtoRequest bookingDtoRequest1 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
        BookingDtoRequest bookingDtoRequest2 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(7));
        BookingDtoRequest bookingDtoRequest3 = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().plusHours(8), LocalDateTime.now().plusHours(15));

        BookingDto bookingDto1 = bookingService.create(bookingDtoRequest1, userDtoResponse2.getId());
        BookingDto bookingDto2 = bookingService.create(bookingDtoRequest2, userDtoResponse2.getId());
        BookingDto bookingDto3 = bookingService.create(bookingDtoRequest3, userDtoResponse2.getId());

        bookingService.changeBookingStatus(userDtoResponse1.getId(), bookingDto1.getId(), false);
        bookingService.changeBookingStatus(userDtoResponse1.getId(), bookingDto2.getId(), false);
        bookingService.changeBookingStatus(userDtoResponse1.getId(), bookingDto3.getId(), false);

        List<BookingDto> bookings = bookingService.getUserItemsBookingsByState(userDtoResponse1.getId(), "REJECTED");

        Assertions.assertThat(bookings.size()).isEqualTo(3);
    }
}

