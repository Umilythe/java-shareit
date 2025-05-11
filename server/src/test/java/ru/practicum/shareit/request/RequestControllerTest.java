package ru.practicum.shareit.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RequestControllerTest {

    @Mock
    ItemRequestService requestService;

    private MockMvc mvc;

    @InjectMocks
    private ItemRequestController controller;
    private final ObjectMapper mapper = new ObjectMapper();

    private UserDto userDto;

    private ItemRequestDtoResponse requestDtoResponse;

    private ItemRequestDto requestDtoRequest;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
        userDto = new UserDto(1L, "Lizzy", "Lizzy@email.com");
        ItemDto item = new ItemDto(1L, "name", "desc", true, UserMapper.toUser(userDto), 1L);
        requestDtoResponse = new ItemRequestDtoResponse(1L, "description", null, List.of(item));
        requestDtoRequest = new ItemRequestDto("desc");
    }

    @Test
    void shouldCreateRequest() throws Exception {
        when(requestService.create(any(ItemRequestDto.class), anyLong()))
                .thenReturn(requestDtoResponse);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("description"));
    }

    @Test
    void shouldReturnRequestById() throws Exception {
        when(requestService.findRequestById(anyLong(), anyLong()))
                .thenReturn(requestDtoResponse);

        mvc.perform(get("/requests/" + requestDtoResponse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("description"));
    }


    @Test
    void shouldReturnBadRequestIfNoUserId() throws Exception {
        mvc.perform(get("/requests/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindAllRequests() throws Exception {
        when(requestService.findAllRequests(anyLong()))
                .thenReturn(List.of(requestDtoResponse));

        mvc.perform(get("/requests/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect((result -> {
                    String json = result.getResponse().getContentAsString();
                    List<ItemRequestDtoResponse> dtos = mapper.readValue(json, new TypeReference<>() {
                    });
                    if (dtos.isEmpty()) {
                        throw new AssertionError("Empty ItemDtoResponse list");
                    }
                }));
    }

    @Test
    void findUserRequests_shouldFindAllUserRequests() throws Exception {
        when(requestService.findUserRequests(anyLong()))
                .thenReturn(List.of(requestDtoResponse));

        mvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect((result -> {
                    String json = result.getResponse().getContentAsString();
                    List<ItemRequestDtoResponse> dtos = mapper.readValue(json, new TypeReference<>() {
                    });
                    if (dtos.isEmpty()) {
                        throw new AssertionError("Empty ItemDtoResponse list");
                    }
                }));
    }

}
