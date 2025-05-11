package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class ItemServiceTest {

    @Autowired
    ItemService itemService;

    @Autowired
    UserService userService;

    @Autowired
    BookingService bookingService;

    @Autowired
    ItemRequestService itemRequestService;

    static UserDto userOne;
    static UserDto userTwo;
    static ItemDto itemDtoOne;
    static ItemDto itemDtoTwo;

    @BeforeAll
    static void init() {
        userOne = new UserDto(null, "userOne", "userOne@email.com");
        userTwo = new UserDto(null, "userTwo", "userTwo@email.com");
        itemDtoOne = new ItemDto(null, "itemOne", "description", true, null, null);
        itemDtoTwo = new ItemDto(null, "itemTwo", "description 2", true, null, null);
    }

    @Test
    void shouldCreateItem() {
        UserDto user = userService.create(userOne);
        ItemDto createdItem = itemService.create(itemDtoOne, user.getId());

        Assertions.assertThat(createdItem.getName()).isEqualTo(itemDtoOne.getName());
        Assertions.assertThat(createdItem.getDescription()).isEqualTo(itemDtoOne.getDescription());
        Assertions.assertThat(createdItem.getAvailable()).isEqualTo(itemDtoOne.getAvailable());
    }

    @Test
    void shouldNotCreateWhenUserNotFound() {
        Assertions.assertThatThrownBy(() -> itemService.create(itemDtoOne, 5L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldUpdateItem() {
        UserDto owner = userService.create(userOne);
        ItemDto createdItem = itemService.create(itemDtoOne, owner.getId());
        ItemDto updatedItem = itemService.update(itemDtoTwo, owner.getId(), createdItem.getId());

        Assertions.assertThat(updatedItem.getName()).isEqualTo(itemDtoTwo.getName());
        Assertions.assertThat(updatedItem.getDescription()).isEqualTo(itemDtoTwo.getDescription());
        Assertions.assertThat(updatedItem.getAvailable()).isEqualTo(itemDtoTwo.getAvailable());
    }

    @Test
    void shouldNotUpdateItem() {
        UserDto owner = userService.create(userOne);
        UserDto user = userService.create(userTwo);
        ItemDto createdItem = itemService.create(itemDtoOne, owner.getId());

        Assertions.assertThatThrownBy(() -> itemService.update(createdItem, user.getId(), createdItem.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenItemNotFound() {
        UserDto owner = userService.create(userOne);

        Assertions.assertThatThrownBy(() -> itemService.update(itemDtoTwo, owner.getId(), 5L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnItemWithBookingsIfOwner() {
        UserDto owner = userService.create(userOne);
        ItemDto item = itemService.create(itemDtoOne, owner.getId());

        ItemDtoWithBooking foundItem = itemService.getById(owner.getId(), item.getId());

        Assertions.assertThat(foundItem.getName()).isEqualTo(item.getName());
        Assertions.assertThat(foundItem.getLastBooking()).isNull();
        Assertions.assertThat(foundItem.getNextBooking()).isNull();
    }

    @Test
    void shouldReturnItemWithoutBookingsIfNotOwner() {
        UserDto owner = userService.create(userOne);
        UserDto anotherUser = userService.create(userTwo);

        ItemDto item = itemService.create(itemDtoOne, owner.getId());

        ItemDtoWithBooking foundItem = itemService.getById(anotherUser.getId(), item.getId());

        Assertions.assertThat(foundItem.getName()).isEqualTo(item.getName());
        Assertions.assertThat(foundItem.getLastBooking()).isNull();
        Assertions.assertThat(foundItem.getNextBooking()).isNull();
    }

    @Test
    void shouldThrowExceptionIfItemNotFound() {
        Assertions.assertThatThrownBy(() -> itemService.getById(1L, 8L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnItemsOfUser() {
        UserDto owner = userService.create(userOne);
        itemService.create(itemDtoOne, owner.getId());
        itemService.create(itemDtoTwo, owner.getId());

        List<ItemDtoWithBooking> items = itemService.getByOwner(owner.getId());

        Assertions.assertThat(items.size()).isEqualTo(2);
    }

    @Test
    void shouldReturnItemsBySearch() {
        UserDto owner = userService.create(userOne);
        itemService.create(itemDtoOne, owner.getId());
        itemService.create(itemDtoTwo, owner.getId());

        List<ItemDto> foundItems = itemService.search("description");

        Assertions.assertThat(foundItems.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldReturnItemsByEmptySearch() {
        UserDto owner = userService.create(userOne);
        itemService.create(itemDtoOne, owner.getId());
        itemService.create(itemDtoTwo, owner.getId());

        List<ItemDto> foundItems = itemService.search(null);

        Assertions.assertThat(foundItems.isEmpty());
    }

    @Test
    void shouldAddCommentWhenBookingExists() {
        UserDto userDtoResponse1 = userService.create(userOne);
        UserDto userDtoResponse2 = userService.create(userTwo);
        ItemDto itemDtoResponse = itemService.create(itemDtoOne, userDtoResponse1.getId());
        BookingDtoRequest bookingDtoRequest = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1));
        BookingDto bookingDtoResponse = bookingService.create(bookingDtoRequest, userDtoResponse2.getId());

        BookingDto bookingApproveDto = bookingService.changeBookingStatus(userDtoResponse1.getId(), bookingDtoResponse.getId(),
                true);

        CommentDtoRequest commentRequest = new CommentDtoRequest("Some text");
        itemService.addComment(commentRequest, itemDtoResponse.getId(), bookingApproveDto.getBooker().getId());

        ItemDtoWithBooking itemWithComments = itemService.getById(userDtoResponse1.getId(), itemDtoResponse.getId());

        Assertions.assertThat(itemWithComments.getComments()).hasSize(1);
        Assertions.assertThat(itemWithComments.getComments().get(0).getText()).isEqualTo("Some text");
    }

    @Test
    void shouldNotAddCommentWhenThereIsNoBooking() {
        UserDto userDtoResponse1 = userService.create(userOne);
        UserDto userDtoResponse2 = userService.create(userTwo);
        UserDto userDtoResponse3 = userService.create(new UserDto(null, "His name", "andSome@Email.com"));
        ItemDto itemDtoResponse = itemService.create(itemDtoOne, userDtoResponse1.getId());
        BookingDtoRequest bookingDtoRequest = new BookingDtoRequest(itemDtoResponse.getId(),
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1));
        BookingDto bookingDtoResponse = bookingService.create(bookingDtoRequest, userDtoResponse2.getId());
        BookingDto bookingApproveDto = bookingService.changeBookingStatus(userDtoResponse1.getId(), bookingDtoResponse.getId(),
                true);

        CommentDtoRequest commentRequest = new CommentDtoRequest("text");
        itemService.addComment(commentRequest, itemDtoResponse.getId(), bookingApproveDto.getBooker().getId());
        Assertions.assertThatThrownBy(() -> itemService.addComment(commentRequest, itemDtoResponse.getId(), userDtoResponse3.getId()))
                .isInstanceOf(ValidationException.class);
    }
}
