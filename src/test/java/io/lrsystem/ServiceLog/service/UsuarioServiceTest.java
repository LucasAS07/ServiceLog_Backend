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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioMapper mapper;
    @Mock private AuthService authService;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RoleRepository roleRepository;

    @InjectMocks private UsuarioService service;

    private static Role role(long id, String nome) {
        Role r = new Role();
        r.setId(id);
        r.setNome(nome);
        return r;
    }

    private static Usuario usuario(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome("Maria");
        u.setEmail("maria@teste.com");
        u.setStatus(true);
        return u;
    }

    private static UsuarioRequestDTO request() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("Maria");
        dto.setEmail("maria@teste.com");
        dto.setSenha("1234");
        dto.setStatus(true);
        dto.setRoleId(2L);
        return dto;
    }

    @Test
    @DisplayName("listar: retorna todos os usuários mapeados")
    void listar_ok() {
        List<Usuario> usuarios = List.of(usuario(1L), usuario(2L));
        List<UsuarioResponseDTO> dtos = List.of(new UsuarioResponseDTO(), new UsuarioResponseDTO());

        when(usuarioRepository.findAll()).thenReturn(usuarios);
        when(mapper.usuariosToDto(usuarios)).thenReturn(dtos);

        assertThat(service.listar()).isSameAs(dtos);
    }

    @Test
    @DisplayName("buscar: retorna o usuário quando existe")
    void buscar_ok() {
        Usuario u = usuario(1L);
        UsuarioResponseDTO dto = new UsuarioResponseDTO();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(mapper.usuarioToDtoResp(u)).thenReturn(dto);

        assertThat(service.buscar(1L)).isSameAs(dto);
    }

    @Test
    @DisplayName("buscar: usuário inexistente -> UsuarioNaoEncontradoException")
    void buscar_naoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L)).isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    @DisplayName("buscar: id nulo -> IllegalArgumentException")
    void buscar_idNulo() {
        assertThatThrownBy(() -> service.buscar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID do usuário não pode ser nulo");

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("salvar: criptografa a senha, associa a role e persiste")
    void salvar_ok() {
        UsuarioRequestDTO dto = request();
        Usuario novo = new Usuario();
        Role roleUser = role(2L, "USER");
        UsuarioResponseDTO resposta = new UsuarioResponseDTO();

        when(roleRepository.findById(2L)).thenReturn(Optional.of(roleUser));
        when(mapper.usuarioToEntity(dto)).thenReturn(novo);
        when(passwordEncoder.encode("1234")).thenReturn("$2a$hash");
        when(usuarioRepository.save(novo)).thenReturn(novo);
        when(mapper.usuarioToDtoResp(novo)).thenReturn(resposta);

        UsuarioResponseDTO resultado = service.salvar(dto);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getSenha()).isEqualTo("$2a$hash").isNotEqualTo("1234");
        assertThat(captor.getValue().getRole()).isSameAs(roleUser);
        assertThat(resultado).isSameAs(resposta);
    }

    @Test
    @DisplayName("salvar: role inexistente -> RoleNaoEncontradaException e nada é salvo")
    void salvar_roleInexistente() {
        when(roleRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.salvar(request())).isInstanceOf(RoleNaoEncontradaException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar: aplica o DTO, re-criptografa a senha, troca a role e persiste")
    void atualizar_ok() {
        UsuarioRequestDTO dto = request();
        dto.setSenha("nova-senha");
        Usuario existente = usuario(1L);
        Role roleUser = role(2L, "USER");
        UsuarioResponseDTO resposta = new UsuarioResponseDTO();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("nova-senha")).thenReturn("$2a$novo-hash");
        when(roleRepository.findById(2L)).thenReturn(Optional.of(roleUser));
        when(mapper.usuarioToDtoResp(existente)).thenReturn(resposta);

        UsuarioResponseDTO resultado = service.atualizar(1L, dto);

        verify(mapper).atualizar(existente, dto);
        verify(usuarioRepository).save(existente);
        assertThat(existente.getSenha()).isEqualTo("$2a$novo-hash");
        assertThat(existente.getRole()).isSameAs(roleUser);
        assertThat(resultado).isSameAs(resposta);
    }

    @Test
    @DisplayName("atualizar: usuário inexistente -> UsuarioNaoEncontradoException")
    void atualizar_usuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(99L, request()))
                .isInstanceOf(UsuarioNaoEncontradoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete: delega ao repositório")
    void delete_ok() {
        service.delete(1L);

        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("inativar: muda status para false")
    void inativar_ok() {
        Usuario u = usuario(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        service.inativar(1L);

        assertThat(u.isStatus()).isFalse();
    }

    @Test
    @DisplayName("ativar: muda status para true")
    void ativar_ok() {
        Usuario u = usuario(1L);
        u.setStatus(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        service.ativar(1L);

        assertThat(u.isStatus()).isTrue();
    }

    @Test
    @DisplayName("inativar/ativar: usuário inexistente -> UsuarioNaoEncontradoException")
    void ativarInativar_usuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.inativar(99L)).isInstanceOf(UsuarioNaoEncontradoException.class);
        assertThatThrownBy(() -> service.ativar(99L)).isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    @DisplayName("myUser: retorna os dados do usuário autenticado")
    void myUser_ok() {
        Usuario u = usuario(5L);
        UsuarioResponseDTO dto = new UsuarioResponseDTO();

        when(authService.authenticated()).thenReturn(u);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(u));
        when(mapper.usuarioToDtoResp(u)).thenReturn(dto);

        assertThat(service.myUser()).isSameAs(dto);
    }
}
