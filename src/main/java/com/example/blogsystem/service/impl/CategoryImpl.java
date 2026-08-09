package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Category;
import com.example.blogsystem.repository.CategoryRepository;
import com.example.blogsystem.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CategoryImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    public  CategoryImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category with id " + id + " does not exist"));
    }
    @Override
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
    @Override
    public Category updateCategory(Long id, Category category) {
        Category existingCategory = getCategoryById(id);
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        return categoryRepository.save(existingCategory);
    }
    @Override
    public void deleteCategory(Long id) {
        if(!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category with id " + id + " does not exist");
        }
        categoryRepository.deleteById(id);
    }
}
