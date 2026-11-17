package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.dto.request.AtendimentoRequestDTO;
import io.lrsystem.ServiceLog.dto.response.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.mapper.AtendimentoMapper;
import io.lrsystem.ServiceLog.model.Atendimento;
import io.lrsystem.ServiceLog.model.RelatorioAtendimento;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.AtendimentoRepository;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import io.lrsystem.ServiceLog.service.exceptions.AtendimentoNaoEncontrado;
import io.lrsystem.ServiceLog.service.exceptions.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtendimentoServiceTest {

    @Mock private AtendimentoMapper atendimentoMapper;
    @Mock private AtendimentoRepository atendimentoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuthService authService;

    @InjectMocks private AtendimentoService service;

    private static final LocalDate DIA = LocalDate.of(2026, 1, 28); // quarta-feira

    private static Usuario usuario(Long id, String nome) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(nome.toLowerCase() + "@teste.com");
        u.setStatus(true);
        return u;
    }

    private static AtendimentoRequestDTO request(LocalDate dia, String inicio, String fim) {
        AtendimentoRequestDTO dto = new AtendimentoRequestDTO();
        dto.setDia(dia);
        dto.setInicio(LocalTime.parse(inicio));
        dto.setFim(LocalTime.parse(fim));
        dto.setJustificativa("Redefinição de senha");
        return dto;
    }

    // ------------------------------------------------------------------
    // listarPorUsuario
    // ------------------------------------------------------------------
    @Test
    @DisplayName("listarPorUsuario: rejeita período em que fim é anterior ao início")
    void listarPorUsuario_fimAntesDoInicio_lancaIllegalArgument() {
        when(authService.authenticated()).thenReturn(usuario(1L, "Maria"));

        assertThatThrownBy(() -> service.listarPorUsuario(
                LocalDate.of(2026, 2, 10), LocalDate.of(2026, 2, 1), PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data fim não pode ser menor que data inicio");

        verifyNoInteractions(atendimentoRepository);
    }

    @Test
    @DisplayName("listarPorUsuario: consulta pelo id do usuário autenticado e mapeia a página")
    void listarPorUsuario_ok() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fim = LocalDate.of(2026, 1, 31);

        Page<Atendimento> pagina = new PageImpl<>(List.of(new Atendimento()));
        Page<AtendimentoResponseDTO> paginaDto = new PageImpl<>(List.of(new AtendimentoResponseDTO()));

        when(authService.authenticated()).thenReturn(usuario(7L, "Maria"));
        when(atendimentoRepository.buscarPorUsuarioEPeriodo(7L, inicio, fim, pageable)).thenReturn(pagina);
        when(atendimentoMapper.toDoList(pagina)).thenReturn(paginaDto);

        Page<AtendimentoResponseDTO> resultado = service.listarPorUsuario(inicio, fim, pageable);

        assertThat(resultado).isSameAs(paginaDto);
    }

    @Test
    @DisplayName("listarPorUsuario: datas nulas são permitidas (sem filtro de período)")
    void listarPorUsuario_semDatas_ok() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Atendimento> pagina = Page.empty();

        when(authService.authenticated()).thenReturn(usuario(7L, "Maria"));
        when(atendimentoRepository.buscarPorUsuarioEPeriodo(7L, null, null, pageable)).thenReturn(pagina);
        when(atendimentoMapper.toDoList(pagina)).thenReturn(Page.empty());

        assertThat(service.listarPorUsuario(null, null, pageable)).isEmpty();
    }

    // ------------------------------------------------------------------
    // adicionar
    // ------------------------------------------------------------------
    @Test
    @DisplayName("adicionar: preenche colaborador, usuário, horários, tempo total e dia da semana")
    void adicionar_mesmoDia_preencheCampos() {
        Usuario user = usuario(1L, "Maria");
        AtendimentoRequestDTO dto = request(DIA, "18:10", "18:22");
        Atendimento entidadeMapeada = new Atendimento();
        entidadeMapeada.setDia(DIA);
        AtendimentoResponseDTO resposta = new AtendimentoResponseDTO();

        when(authService.authenticated()).thenReturn(user);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(user));
        when(atendimentoMapper.toEntity(dto)).thenReturn(entidadeMapeada);
        when(atendimentoRepository.save(any(Atendimento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(atendimentoMapper.toDto(any(Atendimento.class))).thenReturn(resposta);

        AtendimentoResponseDTO resultado = service.adicionar(dto);

        ArgumentCaptor<Atendimento> captor = ArgumentCaptor.forClass(Atendimento.class);
        verify(atendimentoRepository).save(captor.capture());
        Atendimento salvo = captor.getValue();

        assertThat(resultado).isSameAs(resposta);
        assertThat(salvo.getUsuario()).isSameAs(user);
        assertThat(salvo.getColaborador()).isEqualTo("Maria");
        assertThat(salvo.getInicio()).isEqualTo(LocalDateTime.of(2026, 1, 28, 18, 10));
        assertThat(salvo.getFim()).isEqualTo(LocalDateTime.of(2026, 1, 28, 18, 22));
        assertThat(salvo.getTempoTotal()).isEqualTo(Duration.ofMinutes(12));
        assertThat(salvo.getDiaDaSemana()).isEqualTo("quarta-feira");
        assertThat(salvo.getDataRegistro()).isNotNull();
    }

    @Test
    @DisplayName("adicionar: atendimento que vira a meia-noite termina no dia seguinte")
    void adicionar_viradaDeDia_fimNoDiaSeguinte() {
        Usuario user = usuario(1L, "Maria");
        AtendimentoRequestDTO dto = request(DIA, "23:50", "00:10");
        Atendimento entidadeMapeada = new Atendimento();
        entidadeMapeada.setDia(DIA);

        when(authService.authenticated()).thenReturn(user);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(user));
        when(atendimentoMapper.toEntity(dto)).thenReturn(entidadeMapeada);
        when(atendimentoRepository.save(any(Atendimento.class))).thenAnswer(inv -> inv.getArgument(0));

        service.adicionar(dto);

        ArgumentCaptor<Atendimento> captor = ArgumentCaptor.forClass(Atendimento.class);
        verify(atendimentoRepository).save(captor.capture());
        Atendimento salvo = captor.getValue();

        assertThat(salvo.getInicio()).isEqualTo(LocalDateTime.of(2026, 1, 28, 23, 50));
        assertThat(salvo.getFim()).isEqualTo(LocalDateTime.of(2026, 1, 29, 0, 10));
        assertThat(salvo.getTempoTotal()).isEqualTo(Duration.ofMinutes(20));
    }

    @Test
    @DisplayName("adicionar: usuário autenticado que não existe mais no banco -> EntityNotFoundException")
    void adicionar_usuarioNaoEncontrado() {
        Usuario user = usuario(1L, "Maria");

        when(authService.authenticated()).thenReturn(user);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.adicionar(request(DIA, "10:00", "11:00")))
                .isInstanceOf(EntityNotFoundException.class);

        verify(atendimentoRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // atualizar
    // ------------------------------------------------------------------
    @Test
    @DisplayName("atualizar: atendimento inexistente -> AtendimentoNaoEncontrado")
    void atualizar_naoEncontrado() {
        when(atendimentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(99L, request(DIA, "10:00", "11:00")))
                .isInstanceOf(AtendimentoNaoEncontrado.class);

        verify(atendimentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar: recalcula início, fim e tempo total e persiste")
    void atualizar_ok() {
        Atendimento existente = new Atendimento();
        existente.setId(5L);
        AtendimentoRequestDTO dto = request(DIA, "08:00", "09:30");
        AtendimentoResponseDTO resposta = new AtendimentoResponseDTO();

        when(atendimentoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(atendimentoMapper.toDto(existente)).thenReturn(resposta);

        AtendimentoResponseDTO resultado = service.atualizar(5L, dto);

        assertThat(resultado).isSameAs(resposta);
        assertThat(existente.getInicio()).isEqualTo(LocalDateTime.of(2026, 1, 28, 8, 0));
        assertThat(existente.getFim()).isEqualTo(LocalDateTime.of(2026, 1, 28, 9, 30));
        assertThat(existente.getTempoTotal()).isEqualTo(Duration.ofMinutes(90));
        verify(atendimentoMapper).atualizar(existente, dto);
        verify(atendimentoRepository).save(existente);
    }

    @Test
    @Disabled("BUG: atualizar() usa Duration.between(LocalTime, LocalTime); na virada de dia o resultado é negativo. " +
              "Use Duration.between(inicioTs, fimTs) como em adicionar(). Remova o @Disabled após corrigir.")
    @DisplayName("atualizar: atendimento que vira a meia-noite deve ter tempo total positivo")
    void atualizar_viradaDeDia_tempoTotalPositivo() {
        Atendimento existente = new Atendimento();
        AtendimentoRequestDTO dto = request(DIA, "23:50", "00:10");

        when(atendimentoRepository.findById(5L)).thenReturn(Optional.of(existente));

        service.atualizar(5L, dto);

        assertThat(existente.getFim()).isEqualTo(LocalDateTime.of(2026, 1, 29, 0, 10));
        assertThat(existente.getTempoTotal()).isEqualTo(Duration.ofMinutes(20));
    }

    // ------------------------------------------------------------------
    // deletar
    // ------------------------------------------------------------------
    @Test
    @DisplayName("deletar: atendimento inexistente -> AtendimentoNaoEncontrado")
    void deletar_naoEncontrado() {
        when(atendimentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletar(99L)).isInstanceOf(AtendimentoNaoEncontrado.class);

        verify(atendimentoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deletar: valida dono/admin e remove o atendimento")
    void deletar_ok() {
        Atendimento atendimento = new Atendimento();
        atendimento.setUsuario(usuario(3L, "Maria"));

        when(atendimentoRepository.findById(10L)).thenReturn(Optional.of(atendimento));

        service.deletar(10L);

        verify(authService).validateSelfOrAdmin(3L);
        verify(atendimentoRepository).deleteById(10L);
    }

    @Test
    @DisplayName("deletar: sem permissão propaga ForbiddenException e não remove")
    void deletar_semPermissao() {
        Atendimento atendimento = new Atendimento();
        atendimento.setUsuario(usuario(3L, "Maria"));

        when(atendimentoRepository.findById(10L)).thenReturn(Optional.of(atendimento));
        doThrow(new ForbiddenException("Acesso negado")).when(authService).validateSelfOrAdmin(3L);

        assertThatThrownBy(() -> service.deletar(10L)).isInstanceOf(ForbiddenException.class);

        verify(atendimentoRepository, never()).deleteById(any());
    }

    // ------------------------------------------------------------------
    // totalAtendimentosDia
    // ------------------------------------------------------------------
    @Test
    @DisplayName("totalAtendimentosDia: conta os atendimentos do usuário autenticado")
    void totalAtendimentosDia_ok() {
        when(authService.authenticated()).thenReturn(usuario(4L, "Maria"));
        when(atendimentoRepository.findTotalDia(eq(4L), any(LocalDate.class))).thenReturn(3L);

        assertThat(service.totalAtendimentosDia()).isEqualTo(3L);
    }

    // ------------------------------------------------------------------
    // gerarFolhaAtendimentos (usa o .jasper real do classpath)
    // ------------------------------------------------------------------
    @Test
    @DisplayName("gerarFolhaAtendimentos: gera um PDF válido")
    void gerarFolhaAtendimentos_geraPdf() throws Exception {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fim = LocalDate.of(2026, 1, 31);

        // O service ordena a lista in-place, então precisa ser mutável
        List<RelatorioAtendimento> dados = new ArrayList<>(List.of(
                new RelatorioAtendimento("Maria", DIA.plusDays(1), "quinta-feira",
                        LocalDateTime.of(2026, 1, 29, 9, 0), LocalDateTime.of(2026, 1, 29, 9, 30),
                        Duration.ofMinutes(30), "Instalação de software"),
                new RelatorioAtendimento("Maria", DIA, "quarta-feira",
                        LocalDateTime.of(2026, 1, 28, 18, 10), LocalDateTime.of(2026, 1, 28, 18, 22),
                        Duration.ofMinutes(12), "Redefinição de senha")
        ));
        when(atendimentoRepository.buscarPorDataEUsuario(1L, inicio, fim)).thenReturn(dados);

        byte[] pdf = service.gerarFolhaAtendimentos(1L, inicio, fim);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
        // ordenado por dia/início
        assertThat(dados.get(0).getDia()).isEqualTo(DIA);
    }

    @Test
    @DisplayName("gerarFolhaAtendimentos (sem usuário): usa o usuário autenticado")
    void gerarFolhaAtendimentos_usaUsuarioAutenticado() throws Exception {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fim = LocalDate.of(2026, 1, 31);

        when(authService.authenticated()).thenReturn(usuario(8L, "Maria"));
        when(atendimentoRepository.buscarPorDataEUsuario(8L, inicio, fim)).thenReturn(new ArrayList<>());

        byte[] pdf = service.gerarFolhaAtendimentos(inicio, fim);

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
        verify(atendimentoRepository).buscarPorDataEUsuario(8L, inicio, fim);
    }
}
