package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto user);

    UserDto update(Long id, UserDto user);

    UserDto getById(long id);

    List<UserDto> returnAllUsers();

    void remove(long id);
}