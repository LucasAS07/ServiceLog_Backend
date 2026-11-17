package io.lrsystem.ServiceLog.integration;

import io.lrsystem.ServiceLog.model.Atendimento;
import io.lrsystem.ServiceLog.model.RelatorioAtendimento;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.AtendimentoRepository;
import io.lrsystem.ServiceLog.repository.RoleRepository;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Testa as queries JPQL do AtendimentoRepository contra o PostgreSQL real. */
class AtendimentoRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired private AtendimentoRepository atendimentoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RoleRepository roleRepository;

    private final Pageable pagina = PageRequest.of(0, 10, Sort.by("dia"));

    private Usuario salvarUsuario(String nome, String email) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha("hash");
        u.setStatus(true);
        u.setRole(roleRepository.findById(ROLE_USER).orElseThrow());
        return usuarioRepository.save(u);
    }

    private Atendimento salvarAtendimento(Usuario usuario, String dia, String inicio, String fim, String justificativa) {
        LocalDate data = LocalDate.parse(dia);
        LocalTime ini = LocalTime.parse(inicio);
        LocalTime f = LocalTime.parse(fim);

        Atendimento a = new Atendimento();
        a.setColaborador(usuario.getNome());
        a.setDia(data);
        a.setDiaDaSemana(data.getDayOfWeek().name());
        a.setInicio(data.atTime(ini));
        a.setFim(data.atTime(f));
        a.setTempoTotal(Duration.between(ini, f));
        a.setJustificativa(justificativa);
        a.setUsuario(usuario);
        a.setDataRegistro(OffsetDateTime.now());
        return atendimentoRepository.save(a);
    }

    // ------------------------------------------------------------------
    // buscarPorUsuarioEPeriodo
    // ------------------------------------------------------------------
    @Test
    @DisplayName("buscarPorUsuarioEPeriodo: sem datas (null) retorna todos os atendimentos do usuário")
    void buscarPorUsuarioEPeriodo_semFiltros() {
        Usuario maria = salvarUsuario("Maria", "maria@teste.com");
        Usuario joao = salvarUsuario("João", "joao@teste.com");
        salvarAtendimento(maria, "2026-01-10", "09:00", "09:30", "A");
        salvarAtendimento(maria, "2026-01-20", "10:00", "11:00", "B");
        salvarAtendimento(joao, "2026-01-15", "14:00", "15:00", "do João");

        Page<Atendimento> resultado = atendimentoRepository.buscarPorUsuarioEPeriodo(maria.getId(), null, null, pagina);

        assertThat(resultado.getContent()).extracting(Atendimento::getJustificativa).containsExactly("A", "B");
    }

    @Test
    @DisplayName("buscarPorUsuarioEPeriodo: aplica início e/ou fim com limites inclusivos")
    void buscarPorUsuarioEPeriodo_comFiltros() {
        Usuario maria = salvarUsuario("Maria", "maria@teste.com");
        salvarAtendimento(maria, "2026-01-10", "09:00", "09:30", "A");
        salvarAtendimento(maria, "2026-01-20", "10:00", "11:00", "B");
        salvarAtendimento(maria, "2026-01-30", "10:00", "11:00", "C");

        LocalDate d10 = LocalDate.of(2026, 1, 10);
        LocalDate d20 = LocalDate.of(2026, 1, 20);

        assertThat(atendimentoRepository.buscarPorUsuarioEPeriodo(maria.getId(), d20, null, pagina).getContent())
                .extracting(Atendimento::getJustificativa).containsExactly("B", "C");

        assertThat(atendimentoRepository.buscarPorUsuarioEPeriodo(maria.getId(), null, d20, pagina).getContent())
                .extracting(Atendimento::getJustificativa).containsExactly("A", "B");

        assertThat(atendimentoRepository.buscarPorUsuarioEPeriodo(maria.getId(), d10, d20, pagina).getContent())
                .extracting(Atendimento::getJustificativa).containsExactly("A", "B");
    }

    @Test
    @DisplayName("buscarPorUsuarioEPeriodo: respeita a paginação")
    void buscarPorUsuarioEPeriodo_paginacao() {
        Usuario maria = salvarUsuario("Maria", "maria@teste.com");
        salvarAtendimento(maria, "2026-01-10", "09:00", "09:30", "A");
        salvarAtendimento(maria, "2026-01-11", "09:00", "09:30", "B");
        salvarAtendimento(maria, "2026-01-12", "09:00", "09:30", "C");

        Page<Atendimento> segunda = atendimentoRepository.buscarPorUsuarioEPeriodo(
                maria.getId(), null, null, PageRequest.of(1, 2, Sort.by("dia")));

        assertThat(segunda.getTotalElements()).isEqualTo(3);
        assertThat(segunda.getTotalPages()).isEqualTo(2);
        assertThat(segunda.getContent()).extracting(Atendimento::getJustificativa).containsExactly("C");
    }

    // ------------------------------------------------------------------
    // buscarPorDataEUsuario (projeção para o relatório)
    // ------------------------------------------------------------------
    @Test
    @DisplayName("buscarPorDataEUsuario: projeta RelatorioAtendimento ordenado por dia e início")
    void buscarPorDataEUsuario_ordenadoEProjetado() {
        Usuario maria = salvarUsuario("Maria", "maria@teste.com");
        Usuario joao = salvarUsuario("João", "joao@teste.com");
        salvarAtendimento(maria, "2026-01-20", "10:00", "11:00", "terceiro");
        salvarAtendimento(maria, "2026-01-10", "15:00", "15:30", "segundo");
        salvarAtendimento(maria, "2026-01-10", "09:00", "09:30", "primeiro");
        salvarAtendimento(joao, "2026-01-10", "08:00", "08:30", "do João");

        List<RelatorioAtendimento> resultado = atendimentoRepository.buscarPorDataEUsuario(
                maria.getId(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThat(resultado).extracting(RelatorioAtendimento::getJustificativa)
                .containsExactly("primeiro", "segundo", "terceiro");
        assertThat(resultado.get(0).getColaborador()).isEqualTo("Maria");
        assertThat(resultado.get(0).getDia()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(resultado.get(0).getTempoTotal()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("buscarPorDataEUsuario: período exclui registros fora do intervalo")
    void buscarPorDataEUsuario_filtraPeriodo() {
        Usuario maria = salvarUsuario("Maria", "maria@teste.com");
        salvarAtendimento(maria, "2026-01-10", "09:00", "09:30", "dentro");
        salvarAtendimento(maria, "2026-03-10", "09:00", "09:30", "fora");

        List<RelatorioAtendimento> resultado = atendimentoRepository.buscarPorDataEUsuario(
                maria.getId(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThat(resultado).extracting(RelatorioAtendimento::getJustificativa).containsExactly("dentro");
    }

    // ------------------------------------------------------------------
    // findTotalDia
    // ------------------------------------------------------------------
    @Test
    @DisplayName("findTotalDia: conta apenas os atendimentos do usuário naquele dia")
    void findTotalDia() {
        Usuario maria = salvarUsuario("Maria", "maria@teste.com");
        Usuario joao = salvarUsuario("João", "joao@teste.com");
        salvarAtendimento(maria, "2026-01-10", "09:00", "09:30", "1");
        salvarAtendimento(maria, "2026-01-10", "10:00", "10:30", "2");
        salvarAtendimento(maria, "2026-01-11", "10:00", "10:30", "outro dia");
        salvarAtendimento(joao, "2026-01-10", "10:00", "10:30", "do João");

        assertThat(atendimentoRepository.findTotalDia(maria.getId(), LocalDate.of(2026, 1, 10))).isEqualTo(2L);
        assertThat(atendimentoRepository.findTotalDia(maria.getId(), LocalDate.of(2026, 1, 12))).isZero();
    }
}
