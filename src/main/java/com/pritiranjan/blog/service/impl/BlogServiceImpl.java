package com.pritiranjan.blog.service.impl;

import com.pritiranjan.blog.dto.BlogRequestDto;
import com.pritiranjan.blog.dto.BlogResponseDto;
import com.pritiranjan.blog.entity.Blog;
import com.pritiranjan.blog.entity.Category;
import com.pritiranjan.blog.exception.ResourceNotFoundException;
import com.pritiranjan.blog.repository.BlogRepository;
import com.pritiranjan.blog.repository.CategoryRepository;
import com.pritiranjan.blog.service.BlogService;
import com.pritiranjan.blog.util.SlugUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final CategoryRepository categoryRepository;

    public BlogServiceImpl(BlogRepository blogRepository, CategoryRepository categoryRepository) {
        this.blogRepository = blogRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public BlogResponseDto createBlog(BlogRequestDto request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        String slug = generateUniqueSlug(request.getTitle());

        Blog blog = Blog.builder()
                .title(request.getTitle())
                .slug(slug)
                .content(request.getContent())
                .excerpt(request.getExcerpt())
                .thumbnailUrl(request.getThumbnailUrl())
                .published(Boolean.TRUE.equals(request.getIsPublished()))
                .category(category)
                .build();

        Blog saved = blogRepository.save(blog);
        return mapToDto(saved);
    }

    @Override
    public BlogResponseDto updateBlog(Long id, BlogRequestDto request) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setExcerpt(request.getExcerpt());
        blog.setThumbnailUrl(request.getThumbnailUrl());
        blog.setPublished(Boolean.TRUE.equals(request.getIsPublished()));
        blog.setCategory(category);

        if (!blog.getTitle().equals(request.getTitle())) {
            blog.setSlug(generateUniqueSlug(request.getTitle()));
        }

        Blog updated = blogRepository.save(blog);
        return mapToDto(updated);
    }

    @Override
    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));
        blogRepository.delete(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDto> getPublishedBlogs(Pageable pageable) {
        return blogRepository.findByPublishedTrue(pageable)
                .getContent()
                .stream()
                .map(blog -> mapToDto(blog))
                .collect(Collectors.toList());
    }

    @Override
    public BlogResponseDto getBySlug(String slug) {
        Blog blog = blogRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found."));
        return mapToDto(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDto> getByCategorySlug(String categorySlug, Pageable pageable) {
        System.out.println(categorySlug);
        Category category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found for " + categorySlug));
        return blogRepository.findByCategoryAndPublishedTrue(category, pageable)
                .getContent()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = SlugUtil.toSlug(title);
        String slug = baseSlug;
        int counter = 1;
        while (blogRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    private BlogResponseDto mapToDto(Blog blog) {
        return BlogResponseDto.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .content(blog.getContent())
                .excerpt(blog.getExcerpt())
                .thumbnailUrl(blog.getThumbnailUrl())
                .categoryName(blog.getCategory().getName())
                .categorySlug(blog.getCategory().getSlug())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }
}
