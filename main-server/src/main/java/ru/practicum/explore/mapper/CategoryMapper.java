package ru.practicum.explore.mapper;


import ru.practicum.explore.dto.category.CategoryDto;
import ru.practicum.explore.dto.category.NewCategoryDto;
import ru.practicum.explore.model.category.Category;

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