package ru.practicum.explore;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore.dto.category.CategoryDto;
import ru.practicum.explore.dto.category.NewCategoryDto;
import ru.practicum.explore.dto.event.EventFullDto;
import ru.practicum.explore.dto.event.NewEventDto;
import ru.practicum.explore.dto.location.LocationDto;
import ru.practicum.explore.dto.user.NewUserRequest;
import ru.practicum.explore.dto.user.UserDto;
import ru.practicum.explore.repository.category.CategoryRepository;
import ru.practicum.explore.repository.compilation.CompilationRepository;
import ru.practicum.explore.repository.event.EventRepository;
import ru.practicum.explore.repository.participation.ParticipationRequestRepository;
import ru.practicum.explore.repository.user.UserRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected CompilationRepository compilationRepository;

    @Autowired
    protected ParticipationRequestRepository requestRepository;

    protected void clearDatabase() {
        requestRepository.deleteAll();
        compilationRepository.deleteAll();
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected Long createTestUser(String email, String name) throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail(email);
        request.setName(name);

        String response = mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, UserDto.class).getId();
    }

    protected Long createTestCategory(String name) throws Exception {
        NewCategoryDto dto = new NewCategoryDto();
        dto.setName(name);

        String response = mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, CategoryDto.class).getId();
    }

    protected Long createTestEvent(Long userId, Long categoryId, LocalDateTime eventDate) throws Exception {
        NewEventDto eventDto = new NewEventDto();
        eventDto.setAnnotation("Тестовое событие");
        eventDto.setCategory(categoryId);
        eventDto.setDescription("Описание тестового события длиной более 20 символов");
        eventDto.setEventDate(eventDate);
        eventDto.setLocation(new LocationDto(55.754167f, 37.62f));
        eventDto.setPaid(false);
        eventDto.setParticipantLimit(10);
        eventDto.setRequestModeration(true);
        eventDto.setTitle("Тестовое событие");

        String response = mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, EventFullDto.class).getId();
    }
}