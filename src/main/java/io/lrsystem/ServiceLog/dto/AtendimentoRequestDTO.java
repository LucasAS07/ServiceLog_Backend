package io.lrsystem.ServiceLog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AtendimentoRequestDTO {

    @NotNull(message = "Campo obrigatorio")
    private LocalDate dia;

    @NotNull(message = "Campo obrigatorio")
    private LocalTime inicio;

    @NotNull(message = "Campo obrigatorio")
    private LocalTime fim;

    private String justificativa;

    @NotNull(message = "Campo obrigatorio")
    private Long usuarioId;

}
