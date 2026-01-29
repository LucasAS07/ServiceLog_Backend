package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.dto.UsuarioRequestDTO;
import io.lrsystem.ServiceLog.dto.UsuarioResponseDTO;
import io.lrsystem.ServiceLog.exceptions.UsuarioNaoEncontradoException;
import io.lrsystem.ServiceLog.mapper.UsuarioMapper;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioMapper mapper;

    public UsuarioService(UsuarioMapper mapper) {
        this.mapper = mapper;
    }

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<UsuarioResponseDTO> listar() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return mapper.usuariosToDto(usuarios);
    }

    public UsuarioResponseDTO buscar(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        return mapper.usuariroToDto(usuario);
    }

    public UsuarioRequestDTO salvar(Usuario usuario) {
        Usuario usuarioNovo = usuarioRepository.save(usuario);
        UsuarioRequestDTO usuarioDto = mapper.usuarioToDto(usuarioNovo);
        return usuarioDto;
    }

    public UsuarioRequestDTO atualizar(Long id, UsuarioRequestDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(
                () -> new UsuarioNaoEncontradoException("Usuario não encontrado!"));

        mapper.atualizar(usuario,usuarioDTO);
        usuarioRepository.save(usuario);
        UsuarioRequestDTO dto = mapper.usuarioToDto(usuario);
        return dto;
    }

    public void delete(Long id) {
        usuarioRepository.deleteById(id);
    }
}
