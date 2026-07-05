package ru.practicum.server.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.practicum.server.BaseTest;
import ru.practicum.server.dto.category.CategoryDto;
import ru.practicum.server.dto.category.NewCategoryDto;
import ru.practicum.server.dto.event.EventDto;
import ru.practicum.server.dto.event.NewEventDto;
import ru.practicum.server.dto.event.update.UpdateEventAdminRequest;
import ru.practicum.server.dto.location.EventLocationDto;
import ru.practicum.server.dto.user.NewUserRequest;
import ru.practicum.server.dto.user.UserDto;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminEventControllerTest extends BaseTest {

    private Long eventId;
    private Long categoryId;
    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        clearDatabase();

        NewUserRequest userRequest = new NewUserRequest();
        userRequest.setEmail("test@test.com");
        userRequest.setName("Test User");
        String userResponse = mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        userId = objectMapper.readValue(userResponse, UserDto.class).getId();

        NewCategoryDto categoryDto = new NewCategoryDto();
        categoryDto.setName("Концерты");
        String categoryResponse = mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        categoryId = objectMapper.readValue(categoryResponse, CategoryDto.class).getId();

        NewEventDto eventDto = new NewEventDto();
        eventDto.setAnnotation("Тестовое событие для проверки");
        eventDto.setCategory(categoryId);
        eventDto.setDescription("Описание тестового события длиной более 20 символов");
        eventDto.setEventDate(LocalDateTime.now().plusHours(3));
        eventDto.setLocation(new EventLocationDto(55.754167f, 37.62f));
        eventDto.setPaid(false);
        eventDto.setParticipantLimit(10);
        eventDto.setRequestModeration(true);
        eventDto.setTitle("Тестовое событие");

        String eventResponse = mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        eventId = objectMapper.readValue(eventResponse, EventDto.class).getId();
    }

    @Test
    void getEvents_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))));
    }

    @Test
    void getEvents_WithFilters_ShouldReturnFilteredList() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .param("users", userId.toString())
                        .param("states", "PENDING")
                        .param("categories", categoryId.toString())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void publishEvent_ShouldReturnPublished() throws Exception {
        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction("PUBLISH_EVENT");

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedOn").exists());
    }

    @Test
    void rejectEvent_ShouldReturnCanceled() throws Exception {
        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction("REJECT_EVENT");

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("CANCELED"));
    }
}