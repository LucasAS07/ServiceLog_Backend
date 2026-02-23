package io.lrsystem.ServiceLog.controller;

import io.lrsystem.ServiceLog.dto.LoginRequest;
import io.lrsystem.ServiceLog.dto.LoginResponse;
import io.lrsystem.ServiceLog.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Login", description = "Faz login na aplicação")
public class LoginController {

    private final TokenService tokenService;

    @PostMapping("/login")
    @Operation(summary = "Faz Login",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Usuario ou senha invalidos",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    public ResponseEntity<LoginResponse> login(@io.swagger.v3.oas.annotations.parameters.RequestBody
                                                           (description = "Representação de um login",
                                                                   required = true)
                                                @RequestBody LoginRequest request) {
        return ResponseEntity.ok(tokenService.login(request));
    }

}
