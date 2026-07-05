package ru.practicum.server.controller.public_access;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.practicum.server.BaseTest;
import ru.practicum.server.dto.category.CategoryDto;
import ru.practicum.server.dto.category.NewCategoryDto;
import ru.practicum.server.repository.category.CategoryRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PublicCategoryControllerTest extends BaseTest {

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
    void getCategories_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void getCategories_WithPagination_ShouldReturnCorrectPage() throws Exception {
        for (int i = 0; i < 5; i++) {
            NewCategoryDto dto = new NewCategoryDto();
            dto.setName("Категория " + i);
            mockMvc.perform(post("/admin/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));
        }

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(3))));
    }

    @Test
    void getCategory_ShouldReturnCategory() throws Exception {
        mockMvc.perform(get("/categories/{catId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Концерты"));
    }

    @Test
    void getCategory_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/categories/999"))
                .andExpect(status().isNotFound());
    }
}