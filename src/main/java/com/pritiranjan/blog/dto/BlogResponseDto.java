package com.pritiranjan.blog.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class BlogResponseDto {

    private Long id;
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private String thumbnailUrl;
    private String categoryName;
    private String categorySlug;
    private Instant createdAt;
    private Instant updatedAt;
}

