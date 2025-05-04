package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage extends JpaRepository<Item, Long> {
        List<Item> findByOwnerId(Long id);

        @Query("SELECT i FROM Item i " +
                " WHERE (UPPER(i.name) LIKE UPPER(CONCAT('%', ?1, '%'))" +
                " OR UPPER(i.description) LIKE UPPER(CONCAT('%', ?1, '%')))")
        List<Item> search(String text);
}