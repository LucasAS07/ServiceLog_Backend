package io.lrsystem.ServiceLog.model;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RelatorioAtendimento {
    private String colaborador;
    private LocalDate dia;
    private String diaDaSemana;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private Duration tempoTotal;
    private String justificativa;

    public RelatorioAtendimento(
            String colaborador,
            LocalDate dia,
            String diaDaSemana,
            LocalDateTime inicio,
            LocalDateTime fim,
            Duration tempoTotal,
            String justificativa
    ) {
        this.colaborador = colaborador;
        this.dia = dia;
        this.diaDaSemana = diaDaSemana;
        this.inicio = inicio;
        this.fim = fim;
        this.tempoTotal = tempoTotal;
        this.justificativa = justificativa;
    }
}
