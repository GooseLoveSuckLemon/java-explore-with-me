package ru.practicum.explore;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore.repository.category.CategoryRepository;
import ru.practicum.explore.repository.compilation.CompilationRepository;
import ru.practicum.explore.repository.event.EventRepository;
import ru.practicum.explore.repository.participation.ParticipationRequestRepository;
import ru.practicum.explore.repository.user.UserRepository;
import ru.practicum.explore.service.stats.StatsIntegrationService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

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

    @MockBean
    protected StatsIntegrationService statsIntegrationService;

    @BeforeEach
    void setUpMocks() {
        when(statsIntegrationService.getViewsForEvent(anyLong()))
                .thenReturn(0L);

        doNothing().when(statsIntegrationService)
                .sendHit(anyString(), anyString(), anyString(), any());
    }

    protected void clearDatabase() {
        requestRepository.deleteAll();
        compilationRepository.deleteAll();
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }
}
