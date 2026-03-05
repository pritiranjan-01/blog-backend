package com.pritiranjan.blog.service.impl;

import com.pritiranjan.blog.dto.CategoryDto;
import com.pritiranjan.blog.entity.Category;
import com.pritiranjan.blog.exception.BadRequestException;
import com.pritiranjan.blog.exception.ResourceNotFoundException;
import com.pritiranjan.blog.repository.CategoryRepository;
import com.pritiranjan.blog.service.CategoryService;
import com.pritiranjan.blog.util.SlugUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryDto createCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new BadRequestException("Category name already exists");
        }

        String baseSlug = SlugUtil.toSlug(name);
        String uniqueSlug = baseSlug;
        int counter = 1;
        while (categoryRepository.existsBySlug(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter++;
        }

        Category category = Category.builder()
                .name(name)
                .slug(uniqueSlug)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(category);
    }

    private CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .build();
    }
}

