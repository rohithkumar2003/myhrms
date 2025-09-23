package com.example.demo.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.service.UserDetailsServiceImpl;
import com.example.demo.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getServletPath();

        // ✅ Skip JWT validation for forgot-password & OTP APIs
        if (requestPath.contains("/api/auth/forgot-password")
                || requestPath.contains("/api/auth/login")
                || requestPath.contains("/api/employees/login")
                || requestPath.contains("/api/auth/verify-otp")
                || requestPath.contains("/api/auth/reset-password")
        	 || requestPath.startsWith("/api/notices")
        	 || requestPath.startsWith("/holidays")
        	 || requestPath.startsWith("/api/leaves")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        
        

        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        	System.out.println("Missing or invalid Authorization header: " + authHeader);
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7); // remove "Bearer "
        try {
            username = jwtUtil.extractUsername(jwt);
            System.out.println("JWT extracted for username: " + username);  // <-- Add this
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expired");
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            
            System.out.println("Authorities loaded: " + userDetails.getAuthorities()); // <-- Add this
            
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("Authentication set for: " + username); 
            }
            else {
                System.out.println("JWT validation failed for: " + username); // <-- Add this
            }
        }

        filterChain.doFilter(request, response);
    }
}
