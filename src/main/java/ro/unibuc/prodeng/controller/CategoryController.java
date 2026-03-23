package ro.unibuc.prodeng.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import ro.unibuc.prodeng.request.CreateCategoryRequest;
import ro.unibuc.prodeng.response.CategoryResponse;
import ro.unibuc.prodeng.service.CategoryService;

@RestController
@RequestMapping("api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable @NonNull String id) {
        return categoryService.getCategoryById(id);
    }

    @GetMapping("/count")
    public long getTotalCategoriesCount() {
        return categoryService.getTotalCategoriesCount();
    }

    @GetMapping("/user/{userId}/count")
    public long getCategoriesCountByUserId(@PathVariable @NonNull String userId) {
        return categoryService.getCategoriesCountByUserId(userId);
    }

    @GetMapping("/user/{userId}/has-any")
    public boolean hasAnyCategoryForUserId(@PathVariable @NonNull String userId) {
        return categoryService.hasAnyCategoryForUserId(userId);
    }

    @PostMapping
    public CategoryResponse createCategory(@RequestBody CreateCategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable @NonNull String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}
