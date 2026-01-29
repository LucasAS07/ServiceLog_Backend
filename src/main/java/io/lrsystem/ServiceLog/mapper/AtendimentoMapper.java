package io.lrsystem.ServiceLog.mapper;

import io.lrsystem.ServiceLog.dto.AtendimentoRequestDTO;
import io.lrsystem.ServiceLog.dto.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.model.Atendimento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Mapper(componentModel = "spring")
public interface AtendimentoMapper {

    @Mapping(target = "dia", expression = "java(formatarData(atendimento.getDia()))")
    @Mapping(target = "inicio", expression = "java(atendimento.getInicio().toString())")
    @Mapping(target = "fim", expression = "java(atendimento.getFim().toString())")
    @Mapping(
            target = "tempoTotal",
            expression = "java(formatarTempo(atendimento.getTempoTotal()))"
    )
    AtendimentoResponseDTO toDto(Atendimento atendimento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(
            target = "diaDaSemana",
            expression = "java(getDiaDaSemana(dto.getDia()))"
    )
    @Mapping(
            target = "tempoTotal",
            expression = "java(calcularTempoTotal(dto.getInicio(), dto.getFim()))"
    )
    @Mapping(target = "dataRegistro", ignore = true)
    Atendimento toEntity(AtendimentoRequestDTO dto);

    List<AtendimentoResponseDTO> toDoList(List<Atendimento> atendimentos);


    default Duration calcularTempoTotal(LocalTime inicio, LocalTime fim) {
        return Duration.between(inicio, fim);
    }

    default String formatarTempo(Duration duration) {
        if (duration == null) return null;
        long horas = duration.toHours();
        long minutos = duration.minusHours(horas).toMinutes();
        return String.format("%02d:%02d", horas, minutos);
    }

    default String formatarData(LocalDate dia) {
        return dia.format(DateTimeFormatter.ofPattern("dd/MM"));
    }

    default String getDiaDaSemana(LocalDate dia) {
        return dia.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
    }

}
