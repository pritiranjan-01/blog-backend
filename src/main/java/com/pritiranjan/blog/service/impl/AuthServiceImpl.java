package com.pritiranjan.blog.service.impl;

import com.pritiranjan.blog.dto.AuthRequestDto;
import com.pritiranjan.blog.exception.InvalidCredentialsException;
import com.pritiranjan.blog.security.CustomUserDetailsService;
import com.pritiranjan.blog.security.JwtService;
import com.pritiranjan.blog.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           CustomUserDetailsService userDetailsService,
                           JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    public String login(AuthRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        return jwtService.generateToken(userDetails);
    }
}

