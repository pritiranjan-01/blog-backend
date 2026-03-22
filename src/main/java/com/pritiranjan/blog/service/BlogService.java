package com.pritiranjan.blog.service;

import com.pritiranjan.blog.dto.BlogRequestDto;
import com.pritiranjan.blog.dto.BlogResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BlogService {

    BlogResponseDto createBlog(BlogRequestDto request);

    BlogResponseDto updateBlog(Long id, BlogRequestDto request);

    void deleteBlog(Long id);

    List<BlogResponseDto> getPublishedBlogs(Pageable pageable);

    BlogResponseDto getBySlug(String slug);

    List<BlogResponseDto> getByCategorySlug(String categorySlug, Pageable pageable);
}
