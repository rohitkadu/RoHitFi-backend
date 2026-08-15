package com.rohitfi.auth.service;

import com.rohitfi.auth.dto.AuthResponse;
import com.rohitfi.auth.dto.LoginRequest;
import com.rohitfi.auth.dto.RegisterRequest;
import com.rohitfi.auth.entity.User;
import com.rohitfi.auth.repository.UserRepository;
import com.rohitfi.common.util.JwtUtil;
import com.rohitfi.notification.service.EmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByMobile(request.getMobile())) {
            throw new RuntimeException("Mobile number already registered");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .mobile(request.getMobile())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ROLE_CUSTOMER)
                .build();

        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(
                saved.getMobile(),
                saved.getRole().name(),
                saved.getId()
        );

        // Correctly placed inside the method
        emailService.sendWelcomeEmail("rohitkadufreelance@gmail.com", user.getMobile());
        
        return AuthResponse.builder()
                .token(token)
                .role(saved.getRole().name())
                .mobile(saved.getMobile())
                .email(saved.getEmail())
                .userId(saved.getId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new RuntimeException("Invalid mobile or password"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated. Contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid mobile or password");
        }

        String token = jwtUtil.generateToken(
                user.getMobile(),
                user.getRole().name(),
                user.getId()
        );

        // ❌ OLD WAY (Hardcoded for testing)
        // emailService.sendWelcomeEmail("rohitkadufreelance@gmail.com", user.getMobile());

        // ✅ NEW WAY (Sends to the actual registered user)
        emailService.sendWelcomeEmail(user.getEmail(), user.getMobile());
        
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .userId(user.getId())
                .build();
    }
}