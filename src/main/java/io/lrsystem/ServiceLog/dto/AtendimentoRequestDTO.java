package io.lrsystem.ServiceLog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AtendimentoRequestDTO {

    @NotBlank(message = "Campo obrigatorio")
    private LocalDate dia;

    @NotBlank(message = "Campo obrigatorio")
    private LocalTime inicio;

    @NotBlank(message = "Campo obrigatorio")
    private LocalTime fim;

    private String justificativa;

    @NotBlank(message = "Campo obrigatorio")
    private Long usuarioId;

}
