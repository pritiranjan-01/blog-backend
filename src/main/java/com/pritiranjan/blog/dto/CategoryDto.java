package com.pritiranjan.blog.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryDto {

    private Long id;
    private String name;
    private String slug;
}

