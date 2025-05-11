package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody ItemRequestDto itemRequestDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Creating itemRequest {}, userId={}", itemRequestDto, userId);
        return itemRequestClient.create(itemRequestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> returnRequestsOfUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get itemRequests of userId={}", userId);
        return itemRequestClient.findUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> returnAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get all itemRequests");
        return itemRequestClient.findAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> returnRequestById(@PathVariable("requestId") Long requestId,
                                                  @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get itemRequest={}, userId={}", requestId, userId);
        return itemRequestClient.findRequestById(requestId, userId);
    }

}
