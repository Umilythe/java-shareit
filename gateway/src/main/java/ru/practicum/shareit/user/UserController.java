package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.EmptyInformationException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> returnAllUsers() {
        return userClient.returnAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id) {
        return userClient.getById(id);
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto user) {
        if (user.getEmail() == null) {
            log.error("Информация о пользователе должна содержать email.");
            throw new EmptyInformationException("Информация о пользователе должна содержать email.");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Email такого формата не может быть использован.");
            throw new EmptyInformationException("Email такого формата не может быть использован.");
        }
        return userClient.create(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody UserDto newUser) {
        return userClient.update(id, newUser);
    }

    @DeleteMapping("/{id}")
    public void deleteFriend(@PathVariable Long id) {
        userClient.remove(id);
    }
}