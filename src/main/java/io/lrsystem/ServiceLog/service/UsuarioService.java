package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.dto.UsuarioDTO;
import io.lrsystem.ServiceLog.entities.Usuario;
import io.lrsystem.ServiceLog.exceptions.ResourceNotFoundException;
import io.lrsystem.ServiceLog.repositorie.UsuarioRepositorie;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepositorie usuarioRepositorie;

    @Transactional
    public Page<UsuarioDTO> list(Pageable pageable) {
        Page<Usuario> usuarios = usuarioRepositorie.findAll(pageable);

        return usuarios.map(x -> new UsuarioDTO(x));
    }

    @Transactional
    public UsuarioDTO findById(Long id) {
        Usuario usuario = usuarioRepositorie.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Recurso não encontrado"));

        return new UsuarioDTO(usuario);
    }

//    public UsuarioDTO insert() {
//
//    }

    private void copyDtoToEntity(UsuarioDTO usuarioDTO, Usuario usuario) {
        usuario.setNome(usuarioDTO.getNome());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setStatus(usuarioDTO.isStatus());
        usuario.setDataCadastro(usuarioDTO.getDataCadastro());
    }

}
