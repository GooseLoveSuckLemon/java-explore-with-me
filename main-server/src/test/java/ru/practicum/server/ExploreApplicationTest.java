package ru.practicum.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ExploreApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethod() {
        ExploreApplication.main(new String[]{});
    }
}