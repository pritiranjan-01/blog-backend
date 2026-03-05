package com.pritiranjan.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequestDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}

