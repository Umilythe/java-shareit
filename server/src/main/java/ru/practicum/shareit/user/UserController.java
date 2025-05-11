package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<UserDto> returnAllUsers() {
        log.info("Получен запрос на предоставление данных о всех пользователях");
        return userService.returnAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        log.info("Получен запрос на предоставление данных о пользователе с id = " + id);
        return userService.getById(id);
    }

    @PostMapping
    public UserDto create(@RequestBody UserDto user) {
        log.info("Получен запрос на создание пользователя");
        return userService.create(user);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto newUser) {
        log.info("Получен запрос на изменение данных о пользователе с id = " + id);
        return userService.update(id, newUser);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.info("Получен запрос на удаление данных о пользователе с id = " + id);
        userService.remove(id);
    }
}