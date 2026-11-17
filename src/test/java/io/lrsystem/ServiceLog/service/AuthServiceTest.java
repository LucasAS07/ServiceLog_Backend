package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.model.Role;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import io.lrsystem.ServiceLog.service.exceptions.ForbiddenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "maria@teste.com";

    @Mock private UsuarioRepository usuarioRepository;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(usuarioRepository);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String email) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("username", email)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private static Usuario usuario(Long id, String roleNome) {
        Role role = new Role();
        role.setNome(roleNome);

        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(EMAIL);
        u.setRole(role);
        return u;
    }

    @Test
    @DisplayName("authenticated: devolve o usuário referente à claim 'username' do JWT")
    void authenticated_ok() {
        Usuario u = usuario(1L, "USER");
        autenticarComo(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));

        assertThat(service.authenticated()).isSameAs(u);
    }

    @Test
    @DisplayName("authenticated: usuário do token não existe mais -> UsernameNotFoundException")
    void authenticated_usuarioNaoEncontrado() {
        autenticarComo(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticated()).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("authenticated: sem autenticação no contexto -> UsernameNotFoundException")
    void authenticated_semAutenticacao() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> service.authenticated()).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("validateSelfOrAdmin: o próprio usuário é permitido")
    void validateSelfOrAdmin_proprioUsuario() {
        autenticarComo(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(1L, "USER")));

        assertThatCode(() -> service.validateSelfOrAdmin(1L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateSelfOrAdmin: ADMIN pode agir sobre outro usuário")
    void validateSelfOrAdmin_admin() {
        autenticarComo(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(1L, "ADMIN")));

        assertThatCode(() -> service.validateSelfOrAdmin(99L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateSelfOrAdmin: usuário comum sobre outro usuário -> ForbiddenException")
    void validateSelfOrAdmin_negado() {
        autenticarComo(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(1L, "USER")));

        assertThatThrownBy(() -> service.validateSelfOrAdmin(99L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Acesso negado");
    }
}
