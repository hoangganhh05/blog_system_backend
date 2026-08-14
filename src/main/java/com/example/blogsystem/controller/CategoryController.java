package com.example.blogsystem.controller;

import com.example.blogsystem.dto.CategoryDTO;
import com.example.blogsystem.dto.DTOMapper;
import com.example.blogsystem.entity.Category;
import com.example.blogsystem.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/categories", "/api/categories", "/v1/categories", "/api/v1/categories"})
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryDTO> getCategories() {
        return categoryService.getAllCategories().stream()
                .map(DTOMapper::toCategoryDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CategoryDTO getCategoryById(@PathVariable Long id) {
        return DTOMapper.toCategoryDTO(categoryService.getCategoryById(id));
    }

    @PostMapping
    public CategoryDTO createCategory(@RequestBody Category category) {
        return DTOMapper.toCategoryDTO(categoryService.createCategory(category));
    }

    @PutMapping("/{id}")
    public CategoryDTO updateCategory(@PathVariable Long id,
                                   @RequestBody Category category) {
        return DTOMapper.toCategoryDTO(categoryService.updateCategory(id, category));
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}