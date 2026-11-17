package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.dto.request.LoginRequest;
import io.lrsystem.ServiceLog.dto.response.LoginResponse;
import io.lrsystem.ServiceLog.model.Role;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import io.lrsystem.ServiceLog.service.exceptions.UsuarioInativoException;
import io.lrsystem.ServiceLog.service.exceptions.UsuarioOuSenhaInvalidos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private static final String EMAIL = "maria@teste.com";
    private static final String SENHA = "senha-correta";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private JwtEncoder jwtEncoder;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private TokenService service;

    @BeforeEach
    void setUp() {
        // ordem do construtor (@RequiredArgsConstructor): passwordEncoder, usuarioRepository, jwtEncoder
        service = new TokenService(encoder, usuarioRepository, jwtEncoder);
    }

    private Usuario usuario(boolean ativo, String roleNome) {
        Role role = new Role();
        role.setId(2L);
        role.setNome(roleNome);

        Usuario u = new Usuario();
        u.setId(5L);
        u.setNome("Maria");
        u.setEmail(EMAIL);
        u.setSenha(encoder.encode(SENHA));
        u.setStatus(ativo);
        u.setRole(role);
        return u;
    }

    @Test
    @DisplayName("login: credenciais válidas geram token com claims corretas e expiração de 12h")
    void login_ok() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(true, "USER")));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
                Jwt.withTokenValue("token-gerado").header("alg", "RS256").claim("sub", EMAIL).build());

        LoginResponse resposta = service.login(new LoginRequest(EMAIL, SENHA));

        assertThat(resposta.accessToken()).isEqualTo("token-gerado");
        assertThat(resposta.expiresIn()).isEqualTo(43_200L);

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        JwtClaimsSet claims = captor.getValue().getClaims();

        assertThat(claims.getSubject()).isEqualTo(EMAIL);
        assertThat(claims.<String>getClaim("username")).isEqualTo(EMAIL);
        assertThat(claims.<Long>getClaim("usuarioid")).isEqualTo(5L);
        assertThat(claims.<String>getClaim("scope")).isEqualTo("USER");
        assertThat(claims.<String>getClaim("iss")).isEqualTo("backend");
        assertThat(Duration.between(claims.getIssuedAt(), claims.getExpiresAt())).isEqualTo(Duration.ofHours(12));
    }

    @Test
    @DisplayName("login: o escopo do token é o nome da role (ADMIN)")
    void login_escopoAdmin() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(true, "ADMIN")));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
                Jwt.withTokenValue("t").header("alg", "RS256").claim("sub", EMAIL).build());

        service.login(new LoginRequest(EMAIL, SENHA));

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        assertThat(captor.getValue().getClaims().<String>getClaim("scope")).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("login: e-mail inexistente -> UsuarioOuSenhaInvalidos")
    void login_emailInexistente() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest(EMAIL, SENHA)))
                .isInstanceOf(UsuarioOuSenhaInvalidos.class);

        verifyNoInteractions(jwtEncoder);
    }

    @Test
    @DisplayName("login: senha incorreta -> UsuarioOuSenhaInvalidos")
    void login_senhaIncorreta() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(true, "USER")));

        assertThatThrownBy(() -> service.login(new LoginRequest(EMAIL, "outra-senha")))
                .isInstanceOf(UsuarioOuSenhaInvalidos.class);

        verifyNoInteractions(jwtEncoder);
    }

    @Test
    @DisplayName("login: usuário inativo com senha correta -> UsuarioInativoException")
    void login_usuarioInativo() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(false, "USER")));

        assertThatThrownBy(() -> service.login(new LoginRequest(EMAIL, SENHA)))
                .isInstanceOf(UsuarioInativoException.class);

        verifyNoInteractions(jwtEncoder);
    }

    @Test
    @DisplayName("login: usuário inativo com senha errada NÃO revela que está inativo")
    void login_usuarioInativoSenhaErrada_naoVazaStatus() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(false, "USER")));

        assertThatThrownBy(() -> service.login(new LoginRequest(EMAIL, "errada")))
                .isInstanceOf(UsuarioOuSenhaInvalidos.class);
    }
}
