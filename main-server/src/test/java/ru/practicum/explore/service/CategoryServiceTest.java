package ru.practicum.explore.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.explore.dto.category.CategoryDto;
import ru.practicum.explore.dto.category.NewCategoryDto;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.model.category.Category;
import ru.practicum.explore.repository.category.CategoryRepository;
import ru.practicum.explore.repository.event.EventRepository;
import ru.practicum.explore.service.category.CategoryServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_ShouldReturnCategoryDto() {
        NewCategoryDto dto = new NewCategoryDto();
        dto.setName("Концерты");

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = categoryService.createCategory(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Концерты", result.getName());

        verify(categoryRepository).existsByName("Концерты");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_WithDuplicateName_ShouldThrowConflictException() {
        NewCategoryDto dto = new NewCategoryDto();
        dto.setName("Концерты");

        when(categoryRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(dto));

        verify(categoryRepository).existsByName("Концерты");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldReturnUpdatedCategory() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Спорт");

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Спорт")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = categoryService.updateCategory(1L, dto);

        assertNotNull(result);
        assertEquals("Спорт", result.getName());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_WithInvalidId_ShouldThrowNotFoundException() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Спорт");

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.updateCategory(1L, dto));

        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldDeleteCategory() {
        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        when(eventRepository.findByCategoryId(1L)).thenReturn(List.of());

        categoryService.deleteCategory(1L);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_WithInvalidId_ShouldThrowNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.deleteCategory(1L));

        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void getCategories_ShouldReturnCategoryList() {
        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        Page<Category> page = new PageImpl<>(List.of(category));

        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Концерты", result.get(0).getName());

        verify(categoryRepository).findAll(any(Pageable.class));
    }

    @Test
    void getCategory_ShouldReturnCategory() {
        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.getCategory(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Концерты", result.getName());

        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategory_WithInvalidId_ShouldThrowNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.getCategory(1L));

        verify(categoryRepository).findById(1L);
    }
}