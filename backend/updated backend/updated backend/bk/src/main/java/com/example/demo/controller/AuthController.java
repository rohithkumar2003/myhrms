package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.AuthRequest;
import com.example.demo.service.UserDetailsServiceImpl;
import com.example.demo.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // 1️⃣ Authenticate email + password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // 2️⃣ Load user details
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

            // 3️⃣ Generate JWT token
            final String jwt = jwtUtil.generateToken(userDetails);

            // 4️⃣ Return JSON response
            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "email", request.getEmail(),
                    "role", userDetails.getAuthorities().iterator().next().getAuthority()
            ));

        } catch (Exception e) {
            // 5️⃣ Invalid credentials → return 401
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid email or password"
            ));
        }
    }
}
