package com.pritiranjan.blog.controller;

import com.pritiranjan.blog.dto.ApiResponse;
import com.pritiranjan.blog.dto.BlogRequestDto;
import com.pritiranjan.blog.dto.BlogResponseDto;
import com.pritiranjan.blog.service.BlogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    // CREATE BLOG
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<BlogResponseDto>> create(@Valid @RequestBody BlogRequestDto request) {
        BlogResponseDto created = blogService.createBlog(request);
        return new ResponseEntity<>(ApiResponse.success("Blog created successfully", created),HttpStatus.CREATED);
    }

    // UPDATE BLOG
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogResponseDto>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody BlogRequestDto request) {

        BlogResponseDto updated = blogService.updateBlog(id, request);
        return ResponseEntity.ok(ApiResponse.success("Blog updated successsfully", updated));
    }

    // DELETE BLOG
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }

    // GET ALL PUBLISHED BLOGS WITH PAGINATION + SORTING
    @GetMapping
    public ResponseEntity<ApiResponse<List<BlogResponseDto>>> getPublishedBlogs(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "6", required = false) int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc", required = false) String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        List<BlogResponseDto> listPublishedBlogs = blogService.getPublishedBlogs(pageable);

        return ResponseEntity.ok(ApiResponse.success("Blogs fetched successfully", listPublishedBlogs));
    }

    // GET BLOG BY SLUG
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<BlogResponseDto>> getBySlug(@PathVariable("slug") String slug) {
        return ResponseEntity.ok(ApiResponse.success("Blog fetched successfully",blogService.getBySlug(slug)));

    }

    // GET BLOGS BY CATEGORY
    @GetMapping("/category/{slug}")
    public ResponseEntity<ApiResponse<List<BlogResponseDto>>> getByCategory(
            @PathVariable(name = "slug", required = true) String slug,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "6", required = false) int size) {

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity
                .ok(ApiResponse.success("Provided Category Blogs fetched successfully", blogService.getByCategorySlug(slug, pageable)));
    }
}