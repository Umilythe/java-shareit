package ru.practicum.shareit.item.storage;



import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {

    Item create(Item item);

    Item update(Item newItem, Long itemId);

    Optional<Item> getById(Long id);

    List<Item> getByOwner(Long userId);

    List<Item> search(String text);
}