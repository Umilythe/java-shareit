package ru.practicum.shareit.user;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.EmailExistsException;
import ru.practicum.shareit.exceptions.EmptyInformationException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private final UserService userService;
    private UserDto userOne;
    private UserDto userTwo;

    @BeforeEach
    void setUp() {
        userOne = new UserDto(null, "userOne", "userOne@email.com");
        userTwo = new UserDto(null, "userTwo", "userTwo@email.com");
    }

    @Test
    void shouldCreateUser() {
        Assertions.assertThatThrownBy(() -> userService.create(new UserDto(null, "name", "someemail")))
                .isInstanceOf(EmptyInformationException.class);
    }

    @Test
    void shouldNotCreateUser() {
        UserDto userCreated = userService.create(userOne);

        Assertions.assertThat(userCreated.getId()).isNotNull();
        Assertions.assertThat(userCreated.getEmail()).isEqualTo(userOne.getEmail());
        Assertions.assertThat(userCreated.getName()).isEqualTo(userOne.getName());
    }

    @Test
    void shouldUpdateUser() {
        UserDto userCreated = userService.create(userOne);
        UserDto userForUpdate = new UserDto(null, "NewName", "newuser@email.com");
        UserDto userUpdated = userService.update(userCreated.getId(), userForUpdate);

        Assertions.assertThat(userUpdated.getId()).isEqualTo(userCreated.getId());
        Assertions.assertThat(userUpdated.getEmail()).isEqualTo(userForUpdate.getEmail());
        Assertions.assertThat(userUpdated.getName()).isEqualTo(userForUpdate.getName());
    }

    @Test
    void shouldGetById() {
        UserDto userCreated = userService.create(userOne);
        UserDto userFromDb = userService.getById(userCreated.getId());

        Assertions.assertThat(userFromDb.getId()).isEqualTo(userCreated.getId());
        Assertions.assertThat(userFromDb.getEmail()).isEqualTo(userCreated.getEmail());
        Assertions.assertThat(userFromDb.getName()).isEqualTo(userCreated.getName());
    }

    @Test
    void shouldReturnAllUsers() {
        userService.create(userOne);
        userService.create(userTwo);
        List<UserDto> allUsers = userService.returnAllUsers();

        Assertions.assertThat(allUsers).hasSize(2);
    }

    @Test
    void shouldRemoveUserById() {
        UserDto userCreated = userService.create(userOne);
        userService.remove(userCreated.getId());

        Assertions.assertThatThrownBy(() -> userService.getById(userCreated.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldNotCreateUserWithExistingEmail() {
        UserDto userCreated = userService.create(userOne);
        UserDto userForTest = new UserDto(null, "someName", userCreated.getEmail());

        Assertions.assertThatThrownBy(() -> userService.create(userForTest))
                .isInstanceOf(EmailExistsException.class);
    }

    @Test
    void shouldNotCreateUserWithEmptyEmail() {
        UserDto userForTest = new UserDto(null, "someName", null);

        Assertions.assertThatThrownBy(() -> userService.create(userForTest))
                .isInstanceOf(EmptyInformationException.class);
    }

    @Test
    void shouldNotUpdateUserWithStrangeId() {
        UserDto userCreated = userService.create(userOne);
        UserDto userForUpdate = new UserDto(null, "name", "andemail.com");
        Assertions.assertThatThrownBy(() -> userService.update(2L, userForUpdate))
                .isInstanceOf(NotFoundException.class);
    }
}
