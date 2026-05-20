package com.example.focusplatform.controllers;

import com.example.focusplatform.dto.AuthResponse;
import com.example.focusplatform.dto.LoginRequest;
import com.example.focusplatform.dto.RegisterRequest;
import com.example.focusplatform.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request,
                                                 HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);

        // 1. Put the token safely inside the HttpOnly Cookie
        addJwtCookie(response, authResponse.getToken());

        // 2. CRITICAL FIX: Erase the token from the object before sending the JSON back.
        // This ensures React only gets the user's name and role, while the browser handles the token.
        authResponse.setToken(null);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request,
                                              HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);

        // 1. Put the token safely inside the HttpOnly Cookie
        addJwtCookie(response, authResponse.getToken());

        // 2. CRITICAL FIX: Erase the token from the object before sending the JSON back.
        authResponse.setToken(null);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Clear the cookie by setting maxAge to 0
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)          // Set to true in production (HTTPS)
                .path("/")
                .maxAge(0)              // Immediately expire
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    // -----------------------------------------------------------------------
    // Helper: writes the JWT into an HttpOnly cookie so the browser always
    // sends it automatically. "HttpOnly" means JS cannot read it → safer.
    // -----------------------------------------------------------------------
    private void addJwtCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)         // Not accessible via JavaScript
                .secure(false)          // Set to true in production (requires HTTPS)
                .path("/")              // Sent on every request to this server
                .maxAge(10 * 60 * 60)   // 10 hours — matches JWT_EXPIRATION in JwtUtil
                .sameSite("Lax")        // Protects against CSRF while allowing normal navigation
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}