package io.lrsystem.ServiceLog.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AtendimentoRequestDTO {

    private LocalDate dia;
    private LocalTime inicio;
    private LocalTime fim;
    private String justificativa;
    private Long usuarioId;

}
