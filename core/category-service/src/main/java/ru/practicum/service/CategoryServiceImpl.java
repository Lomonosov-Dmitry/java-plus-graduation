package ru.practicum.service;

import dto.category.CategoryDto;
import dto.category.NewCategoryDto;
import exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dal.CategoryRepository;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @Override
    public CategoryDto save(NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.save(categoryMapper.toCategory(newCategoryDto));
        log.info("New category with id {} saved", category.getId());
        return categoryMapper.toCategoryDto(category);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        getCategoryIfExists(id);
        log.info("Category with id {} deleted", id);
        categoryRepository.deleteById(id);
    }

    @Transactional
    @Override
    public CategoryDto update(Long id, NewCategoryDto newCategoryDto) {
        Category category = getCategoryIfExists(id);
        log.info("Updating category - from: {} to: {}", category, newCategoryDto);
        category.setName(newCategoryDto.getName());
        // если есть @Transactional то не надо дергать save репозитория
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.ASC, "id"));
        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getById(Long id) {
        Category category = getCategoryIfExists(id);
        log.info("Category with id {} found: {}", category.getId(), category);
        return categoryMapper.toCategoryDto(category);
    }

    private Category getCategoryIfExists(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category with id %d not found".formatted(id));
        } else return categoryRepository.findById(id).orElseThrow();
    }
}
