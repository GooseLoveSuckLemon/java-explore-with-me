package ru.practicum.server.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.practicum.server.BaseTest;
import ru.practicum.server.dto.category.CategoryDto;
import ru.practicum.server.dto.category.NewCategoryDto;
import ru.practicum.server.dto.event.EventDto;
import ru.practicum.server.dto.event.NewEventDto;
import ru.practicum.server.dto.event.update.EventRequestStatusUpdate;
import ru.practicum.server.dto.event.update.UpdateEventAdminRequest;
import ru.practicum.server.dto.location.EventLocationDto;
import ru.practicum.server.dto.participation.ParticipationRequestDto;
import ru.practicum.server.dto.user.NewUserRequest;
import ru.practicum.server.dto.user.UserDto;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserRequestControllerTest extends BaseTest {

    private Long userId;
    private Long userId2;
    private Long categoryId;
    private Long eventId;

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

        NewUserRequest userRequest2 = new NewUserRequest();
        userRequest2.setEmail("test2@test.com");
        userRequest2.setName("Test User 2");
        String userResponse2 = mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest2)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        userId2 = objectMapper.readValue(userResponse2, UserDto.class).getId();

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

        UpdateEventAdminRequest adminRequest = new UpdateEventAdminRequest();
        adminRequest.setStateAction("PUBLISH_EVENT");
        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PUBLISHED"));
    }

    @Test
    void getUserRequests_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/users/{userId}/requests", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(empty())));
    }

    @Test
    void addParticipationRequest_ShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/users/{userId2}/requests", userId2)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.event").value(eventId))
                .andExpect(jsonPath("$.requester").value(userId2))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void addParticipationRequest_ToOwnEvent_ShouldReturnConflict() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("не может участвовать")));
    }

    @Test
    void addParticipationRequest_AlreadyExists_ShouldReturnConflict() throws Exception {
        mockMvc.perform(post("/users/{userId2}/requests", userId2)
                .param("eventId", eventId.toString()));

        mockMvc.perform(post("/users/{userId2}/requests", userId2)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelRequest_ShouldReturnCanceled() throws Exception {
        String response = mockMvc.perform(post("/users/{userId2}/requests", userId2)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ParticipationRequestDto request = objectMapper.readValue(response, ParticipationRequestDto.class);
        Long requestId = request.getId();

        mockMvc.perform(patch("/users/{userId2}/requests/{requestId}/cancel", userId2, requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void getEventParticipants_ShouldReturnList() throws Exception {
        // Сначала создаем запрос
        mockMvc.perform(post("/users/{userId2}/requests", userId2)
                .param("eventId", eventId.toString()));

        // Потом получаем список участников
        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))));
    }

    @Test
    void changeRequestStatus_ShouldReturnUpdated() throws Exception {
        String response = mockMvc.perform(post("/users/{userId2}/requests", userId2)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ParticipationRequestDto request = objectMapper.readValue(response, ParticipationRequestDto.class);
        Long requestId = request.getId();

        EventRequestStatusUpdate updateRequest = new EventRequestStatusUpdate();
        updateRequest.setRequestIds(java.util.List.of(requestId));
        updateRequest.setStatus("CONFIRMED");

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests", is(not(empty()))));
    }
}