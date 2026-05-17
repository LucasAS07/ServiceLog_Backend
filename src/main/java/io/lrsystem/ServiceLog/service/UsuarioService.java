package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.dto.request.UsuarioRequestDTO;
import io.lrsystem.ServiceLog.dto.response.UsuarioResponseDTO;
import io.lrsystem.ServiceLog.mapper.UsuarioMapper;
import io.lrsystem.ServiceLog.model.Role;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.RoleRepository;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import io.lrsystem.ServiceLog.service.exceptions.RoleNaoEncontradaException;
import io.lrsystem.ServiceLog.service.exceptions.UsuarioNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioMapper mapper;
    private final AuthService authService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;

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
    public UsuarioResponseDTO salvar(UsuarioRequestDTO usuario) {
        Role role = validaBuscaRole(usuario.getRoleId());
        Usuario usuarioNovo = mapper.usuarioToEntity(usuario);
        usuarioNovo.setRole(role);
        usuarioNovo.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return mapper.usuarioToDtoResp(usuarioRepository.save(usuarioNovo));
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO usuarioDTO) {
        Usuario usuario = validaBuscaUsuario(id);

        mapper.atualizar(usuario,usuarioDTO);
        usuario.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        usuario.setRole(validaBuscaRole(usuarioDTO.getRoleId()));
        usuarioRepository.save(usuario);
        return mapper.usuarioToDtoResp(usuario);
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

    @Transactional
    public UsuarioResponseDTO myUser() {
        Usuario usuario = authService.authenticated();
        return buscar(usuario.getId());
    }

    private Usuario validaBuscaUsuario(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
    }

    private Role validaBuscaRole(Long id){
        return roleRepository.findById(id).orElseThrow(
                () -> new RoleNaoEncontradaException("Role não encontrada")
        );
    }
}
