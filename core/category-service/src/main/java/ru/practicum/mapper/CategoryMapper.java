package ru.practicum.mapper;

import dto.category.CategoryDto;
import dto.category.NewCategoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    Category toCategory(NewCategoryDto newCategoryDto);

    CategoryDto toCategoryDto(Category category);
}
