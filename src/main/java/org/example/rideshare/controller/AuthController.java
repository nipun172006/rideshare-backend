package org.example.rideshare.controller;

import jakarta.validation.Valid;
import org.example.rideshare.dto.AuthLoginRequest;
import org.example.rideshare.dto.AuthRegisterRequest;
import org.example.rideshare.dto.AuthResponse;
import org.example.rideshare.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody AuthRegisterRequest req) {
        userService.register(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthLoginRequest req) {
        String token = userService.login(req);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
