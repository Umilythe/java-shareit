package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.EmailExistsException;
import ru.practicum.shareit.exceptions.EmptyInformationException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        validateEmail(userDto.getEmail());
        isEmailUsed(userDto);
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userStorage.save(user));
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден."));
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());
            isEmailUsed(userDto);
            existingUser.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(userStorage.save(existingUser));
    }

    @Override
    @Transactional
    public UserDto getById(Long id) {
        User user = userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден."));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public List<UserDto> returnAllUsers() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void remove(long id) {
        userStorage.deleteById(id);
    }

    private void validateEmail(String email) {
        if (email == null) {
            log.error("Информация о пользователе должна содержать email.");
            throw new EmptyInformationException("Информация о пользователе должна содержать email.");
        }
        if (!email.contains("@")) {
            log.error("Email такого формата не может быть использован.");
            throw new EmptyInformationException("Email такого формата не может быть использован.");
        }
    }

    private void isEmailUsed(UserDto userToCheck) {
        Optional<User> repositoryUser = userStorage.findByEmail(userToCheck.getEmail());
        if (repositoryUser.isPresent() && !repositoryUser.get().getId().equals(userToCheck.getId())) {
            log.error("Такой email " + userToCheck.getEmail() + " уже используется.");
            throw new EmailExistsException("Такой email " + userToCheck.getEmail() + " уже используется.");
        }
    }
}
