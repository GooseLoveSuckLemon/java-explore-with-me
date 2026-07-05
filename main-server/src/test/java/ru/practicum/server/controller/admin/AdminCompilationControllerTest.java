package ru.practicum.server.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.practicum.server.BaseTest;
import ru.practicum.server.dto.compilation.CompilationDto;
import ru.practicum.server.dto.compilation.NewCompilationDto;
import ru.practicum.server.dto.compilation.UpdateCompilationRequest;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminCompilationControllerTest extends BaseTest {

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
    void createCompilation_ShouldReturnCreated() throws Exception {
        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("Зимние события");
        dto.setPinned(false);
        dto.setEvents(List.of());

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Зимние события"))
                .andExpect(jsonPath("$.pinned").value(false));
    }

    @Test
    void updateCompilation_ShouldReturnUpdated() throws Exception {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("Осенние события");
        request.setPinned(false);

        mockMvc.perform(patch("/admin/compilations/{compId}", compilationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compilationId))
                .andExpect(jsonPath("$.title").value("Осенние события"))
                .andExpect(jsonPath("$.pinned").value(false));
    }

    @Test
    void deleteCompilation_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/admin/compilations/{compId}", compilationId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCompilation_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/admin/compilations/999"))
                .andExpect(status().isNotFound());
    }
}