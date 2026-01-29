package io.lrsystem.ServiceLog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Data
public class AtendimentoResponseDTO {

    private Long id;
    private String colaborador;
    private String dia;
    private String diaDaSemana;
    private String inicio;
    private String fim;
    private String tempoTotal;
    private String justificativa;
}
