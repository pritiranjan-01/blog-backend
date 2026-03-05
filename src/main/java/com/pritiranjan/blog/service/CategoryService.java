package com.pritiranjan.blog.service;

import com.pritiranjan.blog.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(String name);

    List<CategoryDto> getAllCategories();

    void deleteCategory(Long id);
}

