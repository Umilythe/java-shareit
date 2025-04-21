package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.EmailExistsException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserStorageImpl implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 1;

    @Override
    public User create(User user) {
        Set<String> emails = users.values()
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toSet());
        isEmailUsed(emails, user);
        user.setId(id);
        id++;
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(Long id, User newUser) {
        checkUserExistence(id);
        Set<String> emails = users.values()
                .stream()
                .filter(userStream -> !Objects.equals(userStream.getId(), id))
                .map(User::getEmail)
                .collect(Collectors.toSet());
        isEmailUsed(emails, newUser);
        User user = users.get(id);
        if (newUser.getName() != null) {
            user.setName(newUser.getName());
        }

        if (newUser.getEmail() != null) {
            user.setEmail(newUser.getEmail());
        }
        users.put(id, user);
        return user;
    }

    @Override
    public void remove(long id) {
        checkUserExistence(id);
        users.remove(id);
    }

    @Override
    public User getById(long id) {
        checkUserExistence(id);
        return users.get(id);
    }

    @Override
    public List<User> returnAllUsers() {
        return new ArrayList<>(users.values());
    }

    private void checkUserExistence(long userId) {
        if (!users.containsKey(userId)) {
            log.error("Пользователя с id= " + userId + " не существует.");
            throw new NotFoundException("Пользователя с id= " + userId + " не существует.");
        }
    }

    private void isEmailUsed(Set<String> emails, User user) {
        if (emails.contains(user.getEmail())) {
            log.error("Такой email уже используется.");
            throw new EmailExistsException("Такой email уже используется.");
        }
    }
}
