package com.example.ordercreator.client;

import com.example.ordercreator.dto.TokenValidationRequest;
import com.example.ordercreator.dto.TokenValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface AuthServiceClient {

    @PostMapping("${auth.service.validate-endpoint}")
    TokenValidationResponse validateToken(@RequestBody TokenValidationRequest request);
}
