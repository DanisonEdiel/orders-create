package com.example.ordercreator.security;

import com.example.ordercreator.dto.TokenValidationResponse;
import com.example.ordercreator.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    @Value("${app.jwt.header}")
    private String headerName;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Extract the JWT token from the request
        String token = extractJwtFromRequest(request);
        
        if (token != null) {
            try {
                // Validate token with Auth service
                TokenValidationResponse validationResponse = authenticationService.validateToken(token);
                
                if (validationResponse != null && validationResponse.isValid()) {
                    // Set authentication in Spring Security context
                    List<SimpleGrantedAuthority> authorities = validationResponse.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                            .collect(Collectors.toList());
                    
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(validationResponse.getUserId(), null, authorities);
                    
                    // Set user attributes in SecurityContext
                    UserPrincipal userPrincipal = new UserPrincipal();
                    userPrincipal.setUserId(validationResponse.getUserId());
                    userPrincipal.setEmail(validationResponse.getEmail());
                    userPrincipal.setRoles(validationResponse.getRoles());
                    
                    authentication.setDetails(userPrincipal);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("Authenticated user {} with roles {}", 
                            validationResponse.getUserId(), validationResponse.getRoles());
                } else {
                    log.warn("Invalid JWT token: {}", validationResponse != null ? validationResponse.getError() : "null response");
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception e) {
                log.error("Authentication failed: {}", e.getMessage(), e);
                SecurityContextHolder.clearContext();
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(headerName);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
