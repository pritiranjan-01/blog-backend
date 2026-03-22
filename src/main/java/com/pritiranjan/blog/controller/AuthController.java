package com.pritiranjan.blog.controller;

import com.pritiranjan.blog.dto.ApiResponse;
import com.pritiranjan.blog.dto.AuthRequestDto;
import com.pritiranjan.blog.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody AuthRequestDto request) {
        String token = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Access token generated successfully", token));
    }
}
