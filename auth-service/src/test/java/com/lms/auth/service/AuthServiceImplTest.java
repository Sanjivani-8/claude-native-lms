package com.lms.auth.service;

import com.lms.auth.dto.AuthResponse;
import com.lms.auth.dto.LoginRequest;
import com.lms.auth.dto.RegisterRequest;
import com.lms.auth.dto.UserResponse;
import com.lms.auth.entity.Role;
import com.lms.auth.entity.User;
import com.lms.auth.repository.UserRepository;
import com.lms.auth.security.JwtUtil;
import com.lms.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("jdoe")
                .email("jdoe@example.com")
                .password("encoded-password")
                .role(Role.STUDENT)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_shouldEncodePasswordAndSaveUser() {
        RegisterRequest request = RegisterRequest.builder()
                .username("jdoe")
                .email("jdoe@example.com")
                .password("plain-password")
                .role("STUDENT")
                .build();

        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jdoe", response.getUsername());
        assertEquals("jdoe@example.com", response.getEmail());
        assertEquals("STUDENT", response.getRole());
    }

    @Test
    void login_shouldReturnTokenOnValidCredentials() {
        LoginRequest request = LoginRequest.builder()
                .username("jdoe")
                .password("plain-password")
                .build();

        when(userRepository.findByUsername("jdoe")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString())).thenReturn("fake-jwt-token");
        when(jwtUtil.extractExpiration("fake-jwt-token")).thenReturn(new Date(System.currentTimeMillis() + 3600000));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("jdoe", response.getUsername());
        assertEquals("STUDENT", response.getRole());
        assertEquals(1L, response.getUserId());
    }
}
