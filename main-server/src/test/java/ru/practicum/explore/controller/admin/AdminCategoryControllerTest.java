package ru.practicum.explore.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.explore.BaseTest;
import ru.practicum.explore.dto.category.CategoryDto;
import ru.practicum.explore.dto.category.NewCategoryDto;
import ru.practicum.explore.repository.category.CategoryRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminCategoryControllerTest extends BaseTest {

    @Autowired
    private CategoryRepository categoryRepository;

    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        clearDatabase();

        NewCategoryDto dto = new NewCategoryDto();
        dto.setName("Концерты");

        String response = mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        CategoryDto categoryDto = objectMapper.readValue(response, CategoryDto.class);
        categoryId = categoryDto.getId();
    }

    @Test
    void addCategory_ShouldReturnCreated() throws Exception {
        NewCategoryDto dto = new NewCategoryDto();
        dto.setName("Спорт");

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Спорт"));
    }

    @Test
    void addCategory_WithDuplicateName_ShouldReturnConflict() throws Exception {
        NewCategoryDto dto = new NewCategoryDto();
        dto.setName("Концерты");

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Категория с названием Концерты уже существует")));
    }

    @Test
    void addCategory_WithInvalidName_ShouldReturnBadRequest() throws Exception {
        NewCategoryDto dto = new NewCategoryDto();
        dto.setName("");

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_ShouldReturnUpdated() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Спорт");

        mockMvc.perform(patch("/admin/categories/{catId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Спорт"));
    }

    @Test
    void updateCategory_WithDuplicateName_ShouldReturnConflict() throws Exception {
        NewCategoryDto newDto = new NewCategoryDto();
        newDto.setName("Выставки");
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isCreated());

        CategoryDto dto = new CategoryDto();
        dto.setName("Выставки");

        mockMvc.perform(patch("/admin/categories/{catId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateCategory_WithInvalidId_ShouldReturnNotFound() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Спорт");

        mockMvc.perform(patch("/admin/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/admin/categories/{catId}", categoryId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategory_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/admin/categories/999"))
                .andExpect(status().isNotFound());
    }
}