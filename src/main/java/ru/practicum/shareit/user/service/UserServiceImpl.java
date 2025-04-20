package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userStorage.create(user));
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User oldUser = userStorage.getById(id);
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
