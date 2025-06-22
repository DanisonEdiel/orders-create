package com.example.ordercreator.service;

import com.example.ordercreator.client.AuthServiceClient;
import com.example.ordercreator.dto.TokenValidationRequest;
import com.example.ordercreator.dto.TokenValidationResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthServiceClient authServiceClient;

    public TokenValidationResponse validateToken(String token) {
        try {
            TokenValidationRequest request = new TokenValidationRequest();
            request.setToken(token);
            TokenValidationResponse response = authServiceClient.validateToken(request);
            log.debug("Token validation response: {}", response);
            return response;
        } catch (FeignException e) {
            log.error("Error validating token: {}", e.getMessage(), e);
            TokenValidationResponse errorResponse = new TokenValidationResponse();
            errorResponse.setValid(false);
            errorResponse.setError("Error validating token: " + e.getMessage());
            return errorResponse;
        }
    }

    public String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
