package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserStorage {

    User create(User user);

    User update(Long id, User newUser);

    void remove(long id);

    User getById(long id);

    List<User> returnAllUsers();
}