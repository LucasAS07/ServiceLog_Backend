package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.dto.UsuarioRequestDTO;
import io.lrsystem.ServiceLog.dto.UsuarioResponseDTO;
import io.lrsystem.ServiceLog.mapper.UsuarioMapper;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import io.lrsystem.ServiceLog.service.exceptions.UsuarioNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioMapper mapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return mapper.usuariosToDto(usuarios);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscar(Long usuarioId) {
        Usuario usuario = validaBuscaUsuario(usuarioId);
        return mapper.usuarioToDtoResp(usuario);
    }

    @Transactional
    public UsuarioResponseDTO salvar(Usuario usuarioId) {
        Usuario usuarioNovo = usuarioRepository.save(usuarioId);
        UsuarioResponseDTO usuarioDto = mapper.usuarioToDtoResp(usuarioNovo);
        return usuarioDto;
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO usuarioDTO) {
        Usuario usuario = validaBuscaUsuario(id);

        mapper.atualizar(usuario,usuarioDTO);
        usuarioRepository.save(usuario);
        UsuarioResponseDTO dto = mapper.usuarioToDtoResp(usuario);
        return dto;
    }

    @Transactional
    public void delete(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public void inativar(Long usuarioId) {
        Usuario usuario = validaBuscaUsuario(usuarioId);

        usuario.setStatus(false);
    }

    @Transactional
    public void ativar(Long usuarioId) {
        Usuario usuario = validaBuscaUsuario(usuarioId);

        usuario.setStatus(true);
    }

    private Usuario validaBuscaUsuario(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
    }
}
