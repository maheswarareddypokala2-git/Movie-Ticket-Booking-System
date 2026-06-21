package com.takehome.moviebooking.controller;

import com.takehome.moviebooking.domain.User;
import com.takehome.moviebooking.repository.UserRepository;
import com.takehome.moviebooking.security.JwtProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody AuthRequest req) {
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(User.Role.CUSTOMER)
                .build();
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest req) {
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow(() -> new RuntimeException("Bad credentials"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Bad credentials");
        }
        return ResponseEntity.ok(jwtProvider.generateToken(user.getId(), user.getRole().name()));
    }

    @Data
    public static class AuthRequest {
        private String name;
        private String email;
        private String password;
    }
}
