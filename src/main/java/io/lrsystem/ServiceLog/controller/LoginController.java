package io.lrsystem.ServiceLog.controller;

import io.lrsystem.ServiceLog.dto.LoginRequest;
import io.lrsystem.ServiceLog.dto.LoginResponse;
import io.lrsystem.ServiceLog.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final TokenService tokenService;

    public LoginController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(tokenService.login(request));
    }

}
