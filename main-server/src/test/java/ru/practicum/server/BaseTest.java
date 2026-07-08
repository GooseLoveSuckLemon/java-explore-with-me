package ru.practicum.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.server.repository.category.CategoryRepository;
import ru.practicum.server.repository.compilation.CompilationRepository;
import ru.practicum.server.repository.event.EventRepository;
import ru.practicum.server.repository.participation.ParticipationRequestRepository;
import ru.practicum.server.repository.user.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;

import java.util.List;

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
    protected StatsClient statsClient;

    @BeforeEach
    void setUpMocks() {

        clearDatabase();

        when(statsClient.getStats(any(), any(), anyList(), anyBoolean()))
                .thenReturn(List.of(new ViewStatsDto("main-service", "/events/7", 1L)));

        doNothing().when(statsClient).sendHit(any());
    }

    protected void clearDatabase() {
        requestRepository.deleteAll();
        compilationRepository.deleteAll();
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }
}
