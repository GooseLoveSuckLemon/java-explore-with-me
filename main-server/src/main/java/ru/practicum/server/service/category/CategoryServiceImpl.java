package ru.practicum.server.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.server.dto.category.CategoryDto;
import ru.practicum.server.dto.category.NewCategoryDto;
import ru.practicum.server.exception.ConflictException;
import ru.practicum.server.exception.NotFoundException;
import ru.practicum.server.mapper.category.CategoryMapper;
import ru.practicum.server.model.category.Category;
import ru.practicum.server.model.event.Event;
import ru.practicum.server.repository.category.CategoryRepository;
import ru.practicum.server.repository.event.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Категория с названием " + dto.getName() + " уже существует");
        }

        Category category = CategoryMapper.toEntity(dto);
        category = categoryRepository.save(category);
        log.info("Created category: {}", category);
        return CategoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto dto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена"));

        if (!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Категория с названием " + dto.getName() + " уже существует");
        }

        category.setName(dto.getName());
        category = categoryRepository.save(category);
        log.info("Категория обновлена: {}", category);
        return CategoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена"));

        List<Event> events = eventRepository.findByCategoryId(categoryId);
        if (!events.isEmpty()) {
            throw new ConflictException("Нельзя удалить категорию, в которой есть события");
        }

        categoryRepository.delete(category);
        log.info("Удалена категория с ID: {}", categoryId);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).getContent().stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена"));
        return CategoryMapper.toDto(category);
    }
}