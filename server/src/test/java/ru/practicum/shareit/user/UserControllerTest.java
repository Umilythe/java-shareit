package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController controller;
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        userDto = new UserDto(
                1L,
                "Annie",
                "annie.deer@mail.com");
    }

    @Test
    void shouldCreateUser() throws Exception {
        when(userService.create(any()))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Annie"))
                .andExpect(jsonPath("$.email").value("annie.deer@mail.com"));
    }

    @Test
    void shouldNotCreateIfBodyIsEmpty() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateUser() throws Exception {
        when(userService.update(anyLong(), any()))
                .thenReturn(userDto);

        mvc.perform(patch("/users/" + userDto.getId())
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Annie"))
                .andExpect(jsonPath("$.email").value("annie.deer@mail.com"));
    }

    @Test
    void shouldRemoveUser() throws Exception {
        mvc.perform(delete("/users/" + userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService, times(1))
                .remove(anyLong());
    }

    @Test
    void shouldGetUserById() throws Exception {
        when(userService.getById(anyLong()))
                .thenReturn(userDto);

        mvc.perform(get("/users/" + userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Annie"))
                .andExpect(jsonPath("$.email").value("annie.deer@mail.com"));
    }

    @Test
    void findAll_shouldFindAllUsers() throws Exception {
        when(userService.returnAllUsers())
                .thenReturn(List.of(userDto));

        mvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Annie"))
                .andExpect(jsonPath("$[0].email").value("annie.deer@mail.com"));
    }

    @Test
    void shouldReturnEmptyListIfNoUsersExist() throws Exception {
        when(userService.returnAllUsers())
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
