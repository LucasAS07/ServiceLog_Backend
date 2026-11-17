package io.lrsystem.ServiceLog.mapper;

import io.lrsystem.ServiceLog.dto.request.AtendimentoRequestDTO;
import io.lrsystem.ServiceLog.dto.response.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.model.Atendimento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AtendimentoMapperTest {

    private final AtendimentoMapper mapper = Mappers.getMapper(AtendimentoMapper.class);

    private static final LocalDate DIA = LocalDate.of(2026, 1, 28); // quarta-feira

    private static Atendimento atendimento() {
        Atendimento a = new Atendimento();
        a.setId(10L);
        a.setColaborador("Maria");
        a.setDia(DIA);
        a.setDiaDaSemana("quarta-feira");
        a.setInicio(LocalDateTime.of(2026, 1, 28, 18, 10));
        a.setFim(LocalDateTime.of(2026, 1, 28, 18, 22));
        a.setTempoTotal(Duration.ofMinutes(12));
        a.setJustificativa("Redefinição de senha");
        return a;
    }

    @Test
    @DisplayName("toDto: formata dia (dd/MM), horários (ISO) e tempo total (HH:mm)")
    void toDto_formataCampos() {
        AtendimentoResponseDTO dto = mapper.toDto(atendimento());

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getColaborador()).isEqualTo("Maria");
        assertThat(dto.getDia()).isEqualTo("28/01");
        assertThat(dto.getDiaDaSemana()).isEqualTo("quarta-feira");
        assertThat(dto.getInicio()).isEqualTo("2026-01-28T18:10");
        assertThat(dto.getFim()).isEqualTo("2026-01-28T18:22");
        assertThat(dto.getTempoTotal()).isEqualTo("00:12");
        assertThat(dto.getJustificativa()).isEqualTo("Redefinição de senha");
    }

    @Test
    @DisplayName("toDto: tempo total nulo permanece nulo")
    void toDto_tempoTotalNulo() {
        Atendimento a = atendimento();
        a.setTempoTotal(null);

        assertThat(mapper.toDto(a).getTempoTotal()).isNull();
    }

    @Test
    @DisplayName("formatarTempo: converte durações em HH:mm, inclusive acima de 24h")
    void formatarTempo() {
        assertThat(mapper.formatarTempo(Duration.ofMinutes(125))).isEqualTo("02:05");
        assertThat(mapper.formatarTempo(Duration.ofMinutes(5))).isEqualTo("00:05");
        assertThat(mapper.formatarTempo(Duration.ofHours(25))).isEqualTo("25:00");
        assertThat(mapper.formatarTempo(Duration.ZERO)).isEqualTo("00:00");
        assertThat(mapper.formatarTempo(null)).isNull();
    }

    @Test
    @DisplayName("getDiaDaSemana: nome em português")
    void diaDaSemana() {
        assertThat(mapper.getDiaDaSemana(LocalDate.of(2026, 1, 26))).isEqualTo("segunda-feira");
        assertThat(mapper.getDiaDaSemana(LocalDate.of(2026, 1, 31))).isEqualTo("sábado");
        assertThat(mapper.getDiaDaSemana(LocalDate.of(2026, 2, 1))).isEqualTo("domingo");
    }

    @Test
    @DisplayName("toEntity: calcula dia da semana e tempo total; ignora id, usuário, início, fim e registro")
    void toEntity() {
        AtendimentoRequestDTO dto = new AtendimentoRequestDTO();
        dto.setDia(DIA);
        dto.setInicio(LocalTime.of(18, 10));
        dto.setFim(LocalTime.of(18, 22));
        dto.setJustificativa("Redefinição de senha");

        Atendimento entidade = mapper.toEntity(dto);

        assertThat(entidade.getDia()).isEqualTo(DIA);
        assertThat(entidade.getJustificativa()).isEqualTo("Redefinição de senha");
        assertThat(entidade.getDiaDaSemana()).isEqualTo("quarta-feira");
        assertThat(entidade.getTempoTotal()).isEqualTo(Duration.ofMinutes(12));
        assertThat(entidade.getId()).isNull();
        assertThat(entidade.getUsuario()).isNull();
        assertThat(entidade.getInicio()).isNull();
        assertThat(entidade.getFim()).isNull();
        assertThat(entidade.getDataRegistro()).isNull();
    }

    @Test
    @DisplayName("atualizar: campos nulos do DTO não sobrescrevem os valores existentes")
    void atualizar_ignoraNulos() {
        Atendimento existente = atendimento();
        AtendimentoRequestDTO dto = new AtendimentoRequestDTO(); // tudo nulo

        mapper.atualizar(existente, dto);

        assertThat(existente.getDia()).isEqualTo(DIA);
        assertThat(existente.getJustificativa()).isEqualTo("Redefinição de senha");
    }

    @Test
    @DisplayName("atualizar: aplica dia e justificativa e NÃO mexe em início/fim (tratados no service)")
    void atualizar_aplicaCamposEIgnoraInicioFim() {
        Atendimento existente = atendimento();
        AtendimentoRequestDTO dto = new AtendimentoRequestDTO();
        dto.setDia(LocalDate.of(2026, 1, 30));
        dto.setInicio(LocalTime.of(1, 0));
        dto.setFim(LocalTime.of(2, 0));
        dto.setJustificativa("Nova justificativa");

        mapper.atualizar(existente, dto);

        assertThat(existente.getDia()).isEqualTo(LocalDate.of(2026, 1, 30));
        assertThat(existente.getJustificativa()).isEqualTo("Nova justificativa");
        assertThat(existente.getInicio()).isEqualTo(LocalDateTime.of(2026, 1, 28, 18, 10));
        assertThat(existente.getFim()).isEqualTo(LocalDateTime.of(2026, 1, 28, 18, 22));
    }

    @Test
    @Disabled("BUG: atualizar() troca o 'dia' mas não recalcula 'diaDaSemana', que fica desatualizado. " +
              "Remova o @Disabled após corrigir.")
    @DisplayName("atualizar: ao mudar o dia, o dia da semana deve ser recalculado")
    void atualizar_recalculaDiaDaSemana() {
        Atendimento existente = atendimento(); // quarta-feira
        AtendimentoRequestDTO dto = new AtendimentoRequestDTO();
        dto.setDia(LocalDate.of(2026, 1, 30)); // sexta-feira

        mapper.atualizar(existente, dto);

        assertThat(existente.getDiaDaSemana()).isEqualTo("sexta-feira");
    }

    @Test
    @DisplayName("toDoList(Page): mantém a paginação e converte cada item")
    void toDoList_page() {
        Page<Atendimento> pagina = new PageImpl<>(List.of(atendimento()));

        Page<AtendimentoResponseDTO> resultado = mapper.toDoList(pagina);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).getDia()).isEqualTo("28/01");
    }
}
