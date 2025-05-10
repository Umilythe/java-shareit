package ru.practicum.shareit.request;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class RequestServiceTest {
    @Autowired
    private final ItemRequestServiceImpl itemRequestService;
    @Autowired
    private final UserServiceImpl userService;
    static UserDto userOne;
    static UserDto userTwo;
    static ItemRequestDto itemRequestDto;

    @BeforeAll
    static void init() {
        userOne = new UserDto(null, "userOne", "userOne@email.com");
        userTwo = new UserDto(null, "userTwo", "userTwo@email.com");
        itemRequestDto = new ItemRequestDto("some description");
    }

    @Test
    void shouldCreateItemRequest() {
        UserDto userDto = userService.create(userOne);
        ItemRequestDtoResponse itemRequestDtoResponse = itemRequestService
                .create(itemRequestDto, userDto.getId());

        Assertions.assertThat(itemRequestDtoResponse.getDescription()).isEqualTo(itemRequestDto.getDescription());
    }

    @Test
    void shouldFindUserRequests() {
        UserDto userDto = userService.create(userOne);
        ItemRequestDtoResponse requestOne = itemRequestService.create(itemRequestDto, userDto.getId());
        ItemRequestDto itemRequestDto1 = new ItemRequestDto("also some description");
        ItemRequestDtoResponse requestTwo = itemRequestService.create(itemRequestDto1, userDto.getId());

        List<ItemRequestDtoResponse> requests = itemRequestService.findUserRequests(userDto.getId());

        Assertions.assertThat(requests.size()).isEqualTo(2);
        Assertions.assertThat(requests.get(0).getId()).isEqualTo(requestTwo.getId());
        Assertions.assertThat(requests.get(1).getId()).isEqualTo(requestOne.getId());
    }

    @Test
    void shouldFindAllRequests() {
        UserDto userDto1 = userService.create(userOne);
        UserDto userDto2 = userService.create(userTwo);
        itemRequestService.create(itemRequestDto, userDto1.getId());
        itemRequestService.create(itemRequestDto, userDto1.getId());
        itemRequestService.create(itemRequestDto, userDto2.getId());
        itemRequestService.create(itemRequestDto, userDto2.getId());

        List<ItemRequestDtoResponse> requests = itemRequestService.findAllRequests(userDto2.getId());

        Assertions.assertThat(requests.size()).isEqualTo(2);
    }

    @Test
    void shouldFindRequestById() {
        UserDto userDto = userService.create(userOne);
        ItemRequestDtoResponse itemRequestDtoResponse = itemRequestService
                .create(itemRequestDto, userDto.getId());
        ItemRequestDtoResponse itemResponseFound = itemRequestService
                .findRequestById(itemRequestDtoResponse.getId(), userDto.getId());

        Assertions.assertThat(itemResponseFound.getDescription()).isEqualTo(itemRequestDto.getDescription());
    }

    @Test
    void shouldNotFindNotUser() {
        Assertions.assertThatThrownBy(() ->
                itemRequestService.findUserRequests(1L)).isInstanceOf(NotFoundException.class);
    }


    @Test
    void shouldReturnEmptyListIfNoRequestsExist() {
        UserDto userDto = userService.create(userOne);
        List<ItemRequestDtoResponse> requests = itemRequestService.findUserRequests(userDto.getId());

        Assertions.assertThat(requests).isEmpty();
    }
}
