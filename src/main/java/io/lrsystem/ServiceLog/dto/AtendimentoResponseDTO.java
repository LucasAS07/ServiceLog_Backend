package io.lrsystem.ServiceLog.dto;

import lombok.Data;

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
