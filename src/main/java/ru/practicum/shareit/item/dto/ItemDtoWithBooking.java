package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.User;

import java.util.List;

@Data
@Builder
public class ItemDtoWithBooking {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private Long requestId;
    private Booking nextBooking;
    private Booking lastBooking;
    private List<CommentDtoResponse> comments;
}
