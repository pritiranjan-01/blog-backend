package com.pritiranjan.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlogRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String excerpt;

    private String thumbnailUrl;

    @NotNull
    private Long categoryId;

    @NotNull
    private Boolean isPublished;
}

