package ru.practicum.shareit.booking;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController controller;

    private MockMvc mvc;

    private User user;
    private Item item;
    private BookingDtoRequest requestDto;
    private BookingDto responseDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mvc = MockMvcBuilders.standaloneSetup(controller).build();

        now = LocalDateTime.now();

        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setName("User Name");

        item = new Item();
        item.setId(1L);
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setAvailable(true);
        item.setOwner(user);

        requestDto = new BookingDtoRequest(1L, now.plusHours(1), now.plusDays(1));

        responseDto = new BookingDto(1L, now.plusHours(1), now.plusDays(1), item, user, Status.WAITING);
    }

    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingService.create(any(BookingDtoRequest.class), anyLong()))
                .thenReturn(responseDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.item.id").value(item.getId()))
                .andExpect(jsonPath("$.booker.id").value(user.getId()))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void shouldApproveBooking() throws Exception {
        when(bookingService.changeBookingStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(responseDto);

        mvc.perform(patch("/bookings/{bookingId}", responseDto.getId())
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.item.id").value(item.getId()))
                .andExpect(jsonPath("$.booker.id").value(user.getId()))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void shouldReturnBooking() throws Exception {
        when(bookingService.getBooking(anyLong(), anyLong()))
                .thenReturn(responseDto);

        mvc.perform(get("/bookings/{bookingId}", responseDto.getId())
                        .header("X-Sharer-User-Id", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.item.id").value(item.getId()))
                .andExpect(jsonPath("$.booker.id").value(user.getId()))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }


    @Test
    void getUserItemsBookingsByState() throws Exception {
        when(bookingService.getUserItemsBookingsByState(anyLong(), any()))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .header("X-Sharer-User-Id", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    List<BookingDto> bookings = mapper.readValue(json, new TypeReference<>() {
                    });
                    if (bookings.isEmpty()) {
                        throw new AssertionError("Booking list is empty");
                    }
                });
    }

    @Test
    void getUserBookingsByState_shouldReturnBookings_whenStateDifferentThanAll() throws Exception {
        when(bookingService.getUserBookingsByState(anyLong(), any()))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/bookings")
                        .param("state", "CURRENT")
                        .header("X-Sharer-User-Id", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()));
    }

    @Test
    void update_shouldReturnBadRequest_whenApprovedParamIsInvalid() throws Exception {
        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .param("approved", "maybe")
                        .header("X-Sharer-User-Id", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}

