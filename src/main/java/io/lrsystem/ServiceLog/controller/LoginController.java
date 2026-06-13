package io.lrsystem.ServiceLog.controller;

import io.lrsystem.ServiceLog.doc.LoginDocController;
import io.lrsystem.ServiceLog.dto.request.LoginRequest;
import io.lrsystem.ServiceLog.dto.response.LoginResponse;
import io.lrsystem.ServiceLog.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginController implements LoginDocController {

    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(tokenService.login(request));
    }

}
