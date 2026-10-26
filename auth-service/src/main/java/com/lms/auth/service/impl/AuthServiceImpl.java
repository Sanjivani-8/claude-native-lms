package com.lms.auth.service.impl;

import com.lms.auth.dto.AuthResponse;
import com.lms.auth.dto.LoginRequest;
import com.lms.auth.dto.RegisterRequest;
import com.lms.auth.dto.UserResponse;
import com.lms.auth.entity.Role;
import com.lms.auth.entity.User;
import com.lms.auth.exception.DuplicateResourceException;
import com.lms.auth.exception.InvalidCredentialsException;
import com.lms.auth.exception.ResourceNotFoundException;
import com.lms.auth.repository.UserRepository;
import com.lms.auth.security.JwtUtil;
import com.lms.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        Role role = resolveRole(request.getRole());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);

        return toUserResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().name());
        long expiresAt = jwtUtil.extractExpiration(token).getTime();

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User '" + username + "' not found"));
        return toUserResponse(user);
    }

    @Override
    public Map<String, Object> validate(String token) {
        Map<String, Object> result = new HashMap<>();

        boolean valid = token != null && jwtUtil.validateToken(token);
        result.put("valid", valid);

        if (valid) {
            result.put("username", jwtUtil.extractUsername(token));
            result.put("role", jwtUtil.extractRole(token));
            result.put("userId", jwtUtil.extractUserId(token));
        } else {
            result.put("username", null);
            result.put("role", null);
            result.put("userId", null);
        }

        return result;
    }

    private Role resolveRole(String roleStr) {
        if (!StringUtils.hasText(roleStr)) {
            return Role.STUDENT;
        }
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Role.STUDENT;
        }
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
