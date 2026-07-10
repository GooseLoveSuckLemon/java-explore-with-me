package ru.practicum.server.mapper.category;

import ru.practicum.server.dto.category.CategoryDto;
import ru.practicum.server.dto.category.NewCategoryDto;
import ru.practicum.server.model.category.Category;

public class CategoryMapper {

    public static Category toEntity(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public static CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
