package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EmailExistsException;
import ru.practicum.shareit.exceptions.EmptyInformationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto create(UserDto userDto) {
        if (userDto.getEmail() == null) {
            log.error("Информация о пользователе должна содержать email.");
            throw new EmptyInformationException("Информация о пользователе должна содержать email.");
        }
        if (!userDto.getEmail().contains("@")) {
            log.error("Email такого формата не может быть использован.");
            throw new EmptyInformationException("Email такого формата не может быть использован.");
        }
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userStorage.create(user));
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User newUser = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userStorage.update(id, newUser));
    }

    @Override
    public UserDto getById(long id) {
        return UserMapper.toUserDto(userStorage.getById(id));
    }

    @Override
    public List<UserDto> returnAllUsers() {
        return userStorage.returnAllUsers().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void remove(long id) {
        userStorage.remove(id);
    }
}
