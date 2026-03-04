package ro.unibuc.prodeng.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.model.CategoryEntity;
import ro.unibuc.prodeng.repository.CategoryRepository;
import ro.unibuc.prodeng.response.CategoryResponse;

@Service
public class CategoryService {
    
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryResponse(category.id(), category.name(), category.assignedUserId()))
                .toList();
    }

    public CategoryResponse getCategoryById(String id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return new CategoryResponse(category.id(), category.name(), category.assignedUserId());
    }

    public CategoryResponse createCategory(String name, String assignedUserId) {
        var category = categoryRepository.save(new CategoryEntity(null, name, assignedUserId));
        return new CategoryResponse(category.id(), category.name(), category.assignedUserId());
    }

    

}
