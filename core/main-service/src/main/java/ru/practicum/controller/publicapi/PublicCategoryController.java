package ru.practicum.controller.publicapi;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategories(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                           @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Getting all categories from {} size {}", from, size);
        return categoryService.getAll(from, size);
    }

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@Positive @PathVariable Long id) {
        log.info("Getting category by id: {}", id);
        return categoryService.getById(id);
    }
}
