package com.pritiranjan.blog.service;

import com.pritiranjan.blog.dto.AuthRequestDto;

public interface AuthService {

    String login(AuthRequestDto request);
}

