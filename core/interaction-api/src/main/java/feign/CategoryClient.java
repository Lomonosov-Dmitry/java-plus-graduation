package feign;

import dto.category.CategoryDto;
import dto.category.NewCategoryDto;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "category-service", path = "/admin/categories")
public interface CategoryClient {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CategoryDto addCategory(@RequestBody @Valid NewCategoryDto newCategoryDto);

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCategory(@PathVariable Long id);

    @PatchMapping("/{id}")
    CategoryDto updateCategory(@PathVariable Long id,
                               @RequestBody @Valid NewCategoryDto newCategoryDto);
}
