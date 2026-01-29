package io.lrsystem.ServiceLog.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.*;

@Entity
@Data
@Table(name = "atendimento")
public class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String colaborador;
    private LocalDate dia;
    private String diaDaSemana;
    private LocalTime inicio;
    private LocalTime fim;

    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    @Column(name = "tempo_total")

    private Duration tempoTotal;
    private String justificativa;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    private OffsetDateTime dataRegistro;

}
