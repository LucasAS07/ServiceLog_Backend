package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.dto.UsuarioCreateUpdateDTO;
import io.lrsystem.ServiceLog.dto.UsuarioResponseDTO;
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
        Usuario usuario = usuarioRepositorie.findById(usuarioId).orElseThrow();
        return mapper.usuariroToDto(usuario);
    }

    public UsuarioResponseDTO salvar(UsuarioCreateUpdateDTO usuario) {

        Usuario novoUsuario = mapper.usuarioToEntity(usuario);

        Usuario usuarioSalvo = usuarioRepositorie.save(novoUsuario);

        return mapper.usuariroToDto(usuarioSalvo);
    }

    public void delete(Long id) {
        UsuarioResponseDTO usuarioResponse = buscar(id);
        Usuario usuario = mapper.toEntity(usuarioResponse);
        usuarioRepositorie.delete(usuario);
    }
}
