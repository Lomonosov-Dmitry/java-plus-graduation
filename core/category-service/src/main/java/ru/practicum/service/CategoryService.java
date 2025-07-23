package ru.practicum.service;


import dto.category.CategoryDto;
import dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto save(NewCategoryDto newCategoryDto);

    void delete(Long id);

    CategoryDto update(Long id, NewCategoryDto newCategoryDto);

    List<CategoryDto> getAll(int from, int size);

    CategoryDto getById(Long id);
}
