package ro.unibuc.prodeng.service;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.model.CategoryEntity;
import ro.unibuc.prodeng.repository.CategoryRepository;
import ro.unibuc.prodeng.request.CreateCategoryRequest;
import ro.unibuc.prodeng.response.CategoryResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

@Service
public class CategoryService {
    
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(@NonNull String id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        return toResponse(category);
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.findByName(request.name()).isPresent()) {
            throw new IllegalArgumentException("Category name already exists: " + request.name());
        }
        CategoryEntity category = new CategoryEntity(
                null,
                request.name(),
                request.assignedUserId()
        );
        CategoryEntity saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    public CategoryResponse changeName(@NonNull String id, String newName) {
        var existing = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        var updated = new CategoryEntity(existing.id(), newName, existing.assignedUserId());
        var saved = categoryRepository.save(updated);
        return toResponse(saved);
    }

    public void deleteCategory(@NonNull String id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException(id);
        }
        categoryRepository.deleteById(id);
    }

    public CategoryResponse assign(@NonNull String id, String userId) {
        var existing = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        var updated = new CategoryEntity(existing.id(), existing.name(), userId);
        var saved = categoryRepository.save(updated);
        return toResponse(saved);
    }

    public CategoryResponse getCategoryByAssignedUserId(@NonNull String userId) {
        var category = categoryRepository.findByAssignedUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(userId));
        return toResponse(category);
    }

    public long getTotalCategoriesCount() {
        return categoryRepository.count();
    }

    public long getCategoriesCountByUserId(@NonNull String userId) {
        return categoryRepository.countByAssignedUserId(userId);
    }

    public boolean hasAnyCategoryForUserId(@NonNull String userId) {
        return categoryRepository.findByAssignedUserId(userId).isPresent();
    }

    private CategoryResponse toResponse(CategoryEntity category) {
        return new CategoryResponse(
                category.id(),
                category.name(),
                category.assignedUserId()
        );
    }

}
