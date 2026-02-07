package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.dto.AtendimentoRequestDTO;
import io.lrsystem.ServiceLog.dto.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.service.exceptions.AtendimentoNaoEncontrado;
import io.lrsystem.ServiceLog.mapper.AtendimentoMapper;
import io.lrsystem.ServiceLog.model.Atendimento;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.AtendimentoRepository;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import io.lrsystem.ServiceLog.service.exceptions.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

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

        atendimento.setTempoTotal(calcularTempoTotal(atendimento.getInicio(),atendimento.getFim()));

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

    private Duration calcularTempoTotal(LocalTime inicio, LocalTime fim) {

        if (fim.isBefore(inicio)) {
            return Duration.between(
                    inicio,
                    fim.plusHours(24)
            );
        }

        return Duration.between(inicio, fim);
    }
}
