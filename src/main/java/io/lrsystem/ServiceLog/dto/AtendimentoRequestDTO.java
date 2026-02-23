package io.lrsystem.ServiceLog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AtendimentoRequestDTO {

    @Schema(example = "2026-01-28")
    @NotNull(message = "Campo obrigatorio")
    private LocalDate dia;

    @Schema(example = "18:10")
    @NotNull(message = "Campo obrigatorio")
    private LocalTime inicio;

    @Schema(example = "18:22")
    @NotNull(message = "Campo obrigatorio")
    private LocalTime fim;

    @Schema(example = "Redefinição de senha")
    private String justificativa;
}
