package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.dto.AtendimentoRequestDTO;
import io.lrsystem.ServiceLog.dto.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.model.RelatorioAtendimento;
import io.lrsystem.ServiceLog.service.exceptions.AtendimentoNaoEncontrado;
import io.lrsystem.ServiceLog.mapper.AtendimentoMapper;
import io.lrsystem.ServiceLog.model.Atendimento;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.AtendimentoRepository;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import io.lrsystem.ServiceLog.service.exceptions.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.sql.Date;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AtendimentoService {

    private final AtendimentoMapper atendimentoMapper;
    private final AtendimentoRepository atendimentoRepository;
    private final UsuarioRepository usuarioRepository;
    private  final AuthService authService;

    @Transactional(readOnly = true)
    public Page<AtendimentoResponseDTO> listarPorUsuario(LocalDate inicio,
                                                         LocalDate fim,
                                                         Pageable pageable) {

        Usuario usuario = authService.authenticated();

        if (inicio != null && fim != null && fim.isBefore(inicio)){
            throw new IllegalArgumentException("Data fim não pode ser menor que data inicio");
        }

        Page<Atendimento> atendimentos =
                atendimentoRepository.buscarPorUsuarioEPeriodo(usuario.getId(),inicio,fim,pageable);
        return atendimentoMapper.toDoList(atendimentos);
    }

    @Transactional
    public AtendimentoResponseDTO adicionar(AtendimentoRequestDTO dto) {

        Usuario user = authService.authenticated();

        Usuario usuario = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado"));

        Atendimento atendimento = atendimentoMapper.toEntity(dto);
        atendimento.setUsuario(usuario);

        atendimento.setColaborador(usuario.getNome());

        inseriTempoTotalInicioEFim(dto, atendimento);

        atendimento.setDiaDaSemana(
                atendimento.getDia()
                        .getDayOfWeek()
                        .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"))
        );

        atendimento.setDataRegistro(OffsetDateTime.now());

        Atendimento atendimentoSalvo = atendimentoRepository.save(atendimento);

        return atendimentoMapper.toDto(atendimentoSalvo);
    }

    @Transactional
    public AtendimentoResponseDTO atualizar(Long id, AtendimentoRequestDTO atendimentoDto) {
        Atendimento atendimentoBusca = atendimentoRepository.findById(id)
                .orElseThrow(() -> new AtendimentoNaoEncontrado("Atendimento não encontrado"));

        LocalDateTime inicioTs = LocalDateTime.of(atendimentoDto.getDia(), atendimentoDto.getInicio());
        LocalDateTime fimTs = LocalDateTime.of(atendimentoDto.getDia(), atendimentoDto.getFim());

        if (atendimentoDto.getFim().isBefore(atendimentoDto.getInicio())){
            fimTs = fimTs.plusDays(1);
        }

        atendimentoBusca.setInicio(inicioTs);
        atendimentoBusca.setFim(fimTs);

        atendimentoBusca.setTempoTotal(Duration.between(atendimentoDto.getInicio(),atendimentoDto.getFim()));

        atendimentoMapper.atualizar(atendimentoBusca,atendimentoDto);
        atendimentoRepository.save(atendimentoBusca);
        AtendimentoResponseDTO dto = atendimentoMapper.toDto(atendimentoBusca);

        return dto;
    }

    @Transactional
    public void deletar(Long id) {

        var atendimento = atendimentoRepository.findById(id).orElseThrow(
                () -> new AtendimentoNaoEncontrado("Atendimento não encontrado")
        );

        authService.validateSelfOrAdmin(atendimento.getUsuario().getId());

        atendimentoRepository.deleteById(id);
    }

    public byte[] gerarFolhaAtendimentos(LocalDate inicio, LocalDate fim) throws JRException {
        Usuario user = authService.authenticated();

        List<RelatorioAtendimento> dados =
                atendimentoRepository.buscarPorDataEUsuario(user.getId(), inicio, fim);

        dados.sort(
                Comparator
                        .comparing(RelatorioAtendimento::getDia)
                        .thenComparing(RelatorioAtendimento::getInicio)
        );

        InputStream logo = getClass()
                .getClassLoader()
                .getResourceAsStream("relatorio/img/logoscmp.png");

        InputStream report = getClass()
                .getClassLoader()
                .getResourceAsStream("relatorio/folha_atendimentos.jasper");

        if (logo == null) {
            throw new RuntimeException("Logo não encontrada");
        }

        if (report == null) {
            throw new RuntimeException("Relatório jasper não encontrado");
        }

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("ID_USUARIO", user.getId());
        parametros.put("DT_INICIO", Date.valueOf(inicio));
        parametros.put("DT_FIM", Date.valueOf(fim));
        parametros.put("REPORT_LOCALE", new Locale("pt", "BR"));
        parametros.put("LOGO", logo);

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                report,
                parametros,
                new JRBeanCollectionDataSource(dados)
        );

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    private static void inseriTempoTotalInicioEFim(AtendimentoRequestDTO dto, Atendimento atendimento) {
        LocalDateTime inicioTs = LocalDateTime.of(dto.getDia(), dto.getInicio());
        LocalDateTime fimTs = LocalDateTime.of(dto.getDia(), dto.getFim());

        if (dto.getFim().isBefore(dto.getInicio())){
            fimTs = fimTs.plusDays(1);
        }

        atendimento.setInicio(inicioTs);
        atendimento.setFim(fimTs);

        atendimento.setTempoTotal(Duration.between(inicioTs,fimTs));
    }
}
