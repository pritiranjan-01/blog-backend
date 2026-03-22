package com.pritiranjan.blog.controller;

import com.pritiranjan.blog.dto.ApiResponse;
import com.pritiranjan.blog.dto.AuthRequestDto;
import com.pritiranjan.blog.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
        
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(true) // Note: Requires HTTPS. For local HTTP dev, you may need to temporarily set this to false.
                .path("/")
                .maxAge(24 * 60 * 60) // 24 hours expiration
                .sameSite("None") // "None" allows cross-origin requests but requires secure(true).
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Access token generated successfully", token));
    }
}
