package io.lrsystem.ServiceLog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@Schema(name = "Problema")
public class CustomError {

    @Schema(example = "2026-02-22T11:21:50.90224598Z")
    private Instant tiemstamp;

    @Schema(example = "403")
    private Integer status;

    @Schema(example = "Usuario ou senha invalidos")
    private String error;

    @Schema(example = "/login")
    private String path;


}
