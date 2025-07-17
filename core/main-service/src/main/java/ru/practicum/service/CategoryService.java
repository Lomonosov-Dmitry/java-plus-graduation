package ru.practicum.service;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto save(NewCategoryDto newCategoryDto);

    void delete(Long id);

    CategoryDto update(Long id, NewCategoryDto newCategoryDto);

    List<CategoryDto> getAll(int from, int size);

    CategoryDto getById(Long id);
}
