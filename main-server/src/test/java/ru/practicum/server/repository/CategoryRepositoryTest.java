package ru.practicum.server.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.server.model.category.Category;
import ru.practicum.server.repository.category.CategoryRepository;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void save_ShouldPersistCategory() {
        Category category = Category.builder()
                .name("Концерты")
                .build();

        Category saved = categoryRepository.save(category);

        assertNotNull(saved.getId());
        assertEquals("Концерты", saved.getName());
    }

    @Test
    void existsByName_ShouldReturnTrue_WhenExists() {
        Category category = Category.builder()
                .name("Концерты")
                .build();
        categoryRepository.save(category);

        boolean exists = categoryRepository.existsByName("Концерты");

        assertTrue(exists);
    }

    @Test
    void existsByName_ShouldReturnFalse_WhenNotExists() {
        boolean exists = categoryRepository.existsByName("Спорт");
        assertFalse(exists);
    }
}