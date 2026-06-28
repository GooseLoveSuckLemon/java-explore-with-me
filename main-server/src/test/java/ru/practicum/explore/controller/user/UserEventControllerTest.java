package ru.practicum.explore.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.explore.BaseTest;
import ru.practicum.explore.dto.category.CategoryDto;
import ru.practicum.explore.dto.category.NewCategoryDto;
import ru.practicum.explore.dto.event.EventFullDto;
import ru.practicum.explore.dto.event.NewEventDto;
import ru.practicum.explore.dto.event.UpdateEventUserRequest;
import ru.practicum.explore.dto.location.LocationDto;
import ru.practicum.explore.dto.user.NewUserRequest;
import ru.practicum.explore.dto.user.UserDto;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserEventControllerTest extends BaseTest {

    private Long userId;
    private Long categoryId;

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
    }

    private NewEventDto createEventDto() {
        NewEventDto dto = new NewEventDto();
        dto.setAnnotation("Тестовое событие для проверки");
        dto.setCategory(categoryId);
        dto.setDescription("Описание тестового события длиной более 20 символов");
        dto.setEventDate(LocalDateTime.now().plusHours(3));
        dto.setLocation(new LocationDto(55.754167f, 37.62f));
        dto.setPaid(false);
        dto.setParticipantLimit(10);
        dto.setRequestModeration(true);
        dto.setTitle("Тестовое событие");
        return dto;
    }

    @Test
    void createEvent_ShouldReturnCreated() throws Exception {
        NewEventDto eventDto = createEventDto();

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.state").value("PENDING"));
    }

    @Test
    void createEvent_WithInvalidDate_ShouldReturnConflict() throws Exception {
        NewEventDto eventDto = createEventDto();
        eventDto.setEventDate(LocalDateTime.now().minusHours(1));

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("at least 2 hours")));
    }

    @Test
    void createEvent_WithInvalidCategory_ShouldReturnNotFound() throws Exception {
        NewEventDto eventDto = createEventDto();
        eventDto.setCategory(999L);

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEvents_ShouldReturnList() throws Exception {
        NewEventDto eventDto = createEventDto();
        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/users/{userId}/events", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))));
    }

    @Test
    void getEvent_ShouldReturnEvent() throws Exception {
        NewEventDto eventDto = createEventDto();
        String response = mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        EventFullDto created = objectMapper.readValue(response, EventFullDto.class);
        Long eventId = created.getId();

        mockMvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.annotation").value("Тестовое событие для проверки"));
    }

    @Test
    void getEvent_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/users/{userId}/events/999", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEvent_ShouldReturnUpdated() throws Exception {
        NewEventDto eventDto = createEventDto();
        String response = mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        EventFullDto created = objectMapper.readValue(response, EventFullDto.class);
        Long eventId = created.getId();

        UpdateEventUserRequest request = new UpdateEventUserRequest();
        request.setAnnotation("Обновленное событие");

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.annotation").value("Обновленное событие"));
    }

    @Test
    void updateEvent_WithInvalidDate_ShouldReturnConflict() throws Exception {
        NewEventDto eventDto = createEventDto();
        String response = mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        EventFullDto created = objectMapper.readValue(response, EventFullDto.class);
        Long eventId = created.getId();

        UpdateEventUserRequest request = new UpdateEventUserRequest();
        request.setEventDate(LocalDateTime.now().minusHours(1));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("at least 2 hours")));
    }
}