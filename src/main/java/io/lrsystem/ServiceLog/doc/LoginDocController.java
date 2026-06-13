package io.lrsystem.ServiceLog.doc;

import io.lrsystem.ServiceLog.dto.request.LoginRequest;
import io.lrsystem.ServiceLog.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Login", description = "Faz login na aplicação")
public interface LoginDocController {

    @Operation(summary = "Faz Login",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Usuario ou senha invalidos",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    ResponseEntity<LoginResponse> login(@io.swagger.v3.oas.annotations.parameters.RequestBody
                                                       (description = "Representação de um login",
                                                               required = true)
                                               @RequestBody LoginRequest request);
}
