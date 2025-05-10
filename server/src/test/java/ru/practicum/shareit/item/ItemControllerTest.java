package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {

    @Mock
    ItemServiceImpl itemService;

    @InjectMocks
    private ItemController itemController;
    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mvc;
    private UserDto userDto;
    private ItemDtoWithBooking itemDtoResponse;
    private ItemDto itemDto;
    private Item item;
    private CommentDtoResponse commentDtoResponse;
    private CommentDtoRequest commentDtoRequest;


    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        mvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .build();

        userDto = new UserDto(1L, "User", "user@mail.com");

        itemDto = new ItemDto(null, "name", "description", true, null, null);

        itemDtoResponse = new ItemDtoWithBooking(1L, "name", "description", true,
                null, null, null, null, null);

        User user = new User(1L, "Jenny", "jenny@mail.com");
        item = new Item(1L, "item", "description", true, user, null);

        commentDtoRequest = new CommentDtoRequest("description");

        commentDtoResponse = new CommentDtoResponse(1L, "text", item, "name", now);
    }

    @Test
    void shouldCreateItem() throws Exception {
        when(itemService.create(any(), anyLong()))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDtoResponse))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.description").value("description"));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        when(itemService.update(any(), anyLong(), anyLong()))
                .thenReturn(itemDto);

        mvc.perform(patch("/items/" + itemDtoResponse.getId())
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.description").value("description"));
    }

    @Test
    void findById_shouldFindItemById() throws Exception {
        when(itemService.getById(anyLong(), anyLong()))
                .thenReturn(itemDtoResponse);

        mvc.perform(get("/items/" + itemDtoResponse.getId())
                        .header("X-Sharer-User-Id", 123L)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.description").value("description"));
    }

    @Test
    void shouldFindItemByOwnerId() throws Exception {
        when(itemService.getByOwner(anyLong()))
                .thenReturn(List.of(itemDtoResponse));

        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect((result -> {
                    String json = result.getResponse().getContentAsString();
                    List<ItemDtoWithBooking> dtos = mapper.readValue(json, new TypeReference<>() {
                    });
                    if (dtos.isEmpty()) {
                        throw new AssertionError("Empty ItemDtoResponse list");
                    }
                }));
    }

    @Test
    void shouldFindItemBySearch() throws Exception {
        when(itemService.search(anyString()))
                .thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search?text=" + anyString())
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect((result -> {
                    String json = result.getResponse().getContentAsString();
                    List<ItemDto> dtos = mapper.readValue(json, new TypeReference<>() {
                    });
                    if (dtos.isEmpty()) {
                        throw new AssertionError("Empty ItemDtoResponse list");
                    }
                }));
    }

    @Test
    void shouldAddComment() throws Exception {
        when(itemService.addComment(any(), anyLong(), anyLong()))
                .thenReturn(commentDtoResponse);

        mvc.perform(post("/items/" + itemDtoResponse.getId() + "/comment")
                        .content(mapper.writeValueAsString(commentDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("text"))
                .andExpect(jsonPath("$.item").value(item))
                .andExpect(jsonPath("$.authorName").value("name"));
    }

}
