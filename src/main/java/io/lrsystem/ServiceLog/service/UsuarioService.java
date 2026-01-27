package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.dto.UsuarioCreateUpdateDTO;
import io.lrsystem.ServiceLog.dto.UsuarioResponseDTO;
import io.lrsystem.ServiceLog.exceptions.UsuarioNaoEncontradoException;
import io.lrsystem.ServiceLog.mapper.UsuarioMapper;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repositorie.UsuarioRepositorie;
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
    private UsuarioRepositorie usuarioRepositorie;

    public List<UsuarioResponseDTO> listar() {
        List<Usuario> usuarios = usuarioRepositorie.findAll();
        return mapper.usuariosToDto(usuarios);
    }

    public UsuarioResponseDTO buscar(Long usuarioId) {
        Usuario usuario = usuarioRepositorie.findById(usuarioId).orElse(null);
        return mapper.usuariroToDto(usuario);
    }

    public UsuarioCreateUpdateDTO salvar(Usuario usuario) {
        Usuario usuarioNovo = usuarioRepositorie.save(usuario);
        UsuarioCreateUpdateDTO usuarioDto = mapper.usuarioToDto(usuarioNovo);
        return usuarioDto;
    }

    public UsuarioCreateUpdateDTO atualizar(Long id, UsuarioCreateUpdateDTO usuarioDTO) {
        Usuario usuario = usuarioRepositorie.findById(id).orElseThrow(
                () -> new UsuarioNaoEncontradoException("Usuario não encontrado!"));

        mapper.atualizar(usuario,usuarioDTO);
        usuarioRepositorie.save(usuario);
        UsuarioCreateUpdateDTO dto = mapper.usuarioToDto(usuario);
        return dto;
    }

    public void delete(Long id) {
        usuarioRepositorie.deleteById(id);
    }
}
