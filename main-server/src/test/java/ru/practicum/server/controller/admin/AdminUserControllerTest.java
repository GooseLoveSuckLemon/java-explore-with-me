package ru.practicum.server.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.practicum.server.BaseTest;
import ru.practicum.server.dto.user.NewUserRequest;
import ru.practicum.server.dto.user.UserDto;
import ru.practicum.server.repository.user.UserRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminUserControllerTest extends BaseTest {

    @Autowired
    private UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        clearDatabase();

        NewUserRequest request = new NewUserRequest();
        request.setEmail("test@test.com");
        request.setName("Test User");

        String response = mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserDto userDto = objectMapper.readValue(response, UserDto.class);
        userId = userDto.getId();
    }

    @Test
    void createUser_ShouldReturnCreated() throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("test2@test.com");
        request.setName("Test User 2");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test2@test.com"))
                .andExpect(jsonPath("$.name").value("Test User 2"));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("test@test.com");
        request.setName("Test User 2");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("уже существует")));
    }

    @Test
    void getUsers_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void getUsers_WithIds_ShouldReturnFilteredList() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("ids", userId.toString())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").value(userId));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/admin/users/999"))
                .andExpect(status().isNotFound());
    }
}