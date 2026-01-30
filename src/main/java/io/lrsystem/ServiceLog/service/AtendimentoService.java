package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.dto.AtendimentoRequestDTO;
import io.lrsystem.ServiceLog.dto.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.exceptions.AtendimentoNaoEncontrado;
import io.lrsystem.ServiceLog.mapper.AtendimentoMapper;
import io.lrsystem.ServiceLog.model.Atendimento;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.AtendimentoRepository;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AtendimentoService {

    private final AtendimentoMapper atendimentoMapper;
    private final AtendimentoRepository atendimentoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<AtendimentoResponseDTO> listarPorUsuario(Long usuarioId) {
        List<Atendimento> atendimentos = atendimentoRepository.findByUsuarioId(usuarioId);
        return atendimentoMapper.toDoList(atendimentos);
    }

    @Transactional
    public AtendimentoResponseDTO adicionar(AtendimentoRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
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
