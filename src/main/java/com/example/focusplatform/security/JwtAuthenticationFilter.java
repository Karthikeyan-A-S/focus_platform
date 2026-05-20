package com.example.focusplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;

        // 1. First, try to get the token from the Authorization header (for API clients / Postman)
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        // 2. If not in the header, look inside the HttpOnly cookie (for browser clients)
        if (jwt == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // 3. If we still have no token, skip authentication and move on
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Extract the email from the token
        final String userEmail;
        try {
            userEmail = jwtUtil.extractEmail(jwt);
        } catch (Exception e) {
            // Token is malformed — treat as unauthenticated
            filterChain.doFilter(request, response);
            return;
        }

        // 5. If we have an email and the user isn't already authenticated in this session
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.isTokenValid(jwt, userEmail)) {
                // Extract the role from the token
                String role = jwtUtil.extractClaim(jwt, claims -> claims.get("role", String.class));

                // Create the authentication token for Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(role))
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Save the user in the security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue processing the request
        filterChain.doFilter(request, response);
    }
}