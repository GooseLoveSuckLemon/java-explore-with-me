package ru.practicum.server.controller.public_access;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.practicum.server.BaseTest;
import ru.practicum.server.dto.compilation.CompilationDto;
import ru.practicum.server.dto.compilation.NewCompilationDto;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PublicCompilationControllerTest extends BaseTest {

    private Long compilationId;

    @BeforeEach
    void setUp() throws Exception {
        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("Летние события");
        dto.setPinned(true);
        dto.setEvents(List.of());

        String response = mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        CompilationDto compilationDto = objectMapper.readValue(response, CompilationDto.class);
        compilationId = compilationDto.getId();
    }

    @Test
    void getCompilations_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/compilations")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].pinned").exists())
                .andExpect(jsonPath("$[0].events").exists());
    }

    @Test
    void getCompilations_WithPinnedFilter_ShouldReturnFiltered() throws Exception {
        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$[0].pinned").value(true));
    }

    @Test
    void getCompilations_WithPagination_ShouldReturnCorrectPage() throws Exception {
        for (int i = 0; i < 5; i++) {
            NewCompilationDto dto = new NewCompilationDto();
            dto.setTitle("Подборка " + i);
            dto.setPinned(false);
            dto.setEvents(List.of());
            mockMvc.perform(post("/admin/compilations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));
        }

        mockMvc.perform(get("/compilations")
                        .param("from", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(3))));
    }

    @Test
    void getCompilation_ShouldReturnCompilation() throws Exception {
        mockMvc.perform(get("/compilations/{compId}", compilationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compilationId))
                .andExpect(jsonPath("$.title").value("Летние события"))
                .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    void getCompilation_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/compilations/999"))
                .andExpect(status().isNotFound());
    }
}