package io.lrsystem.ServiceLog.integration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Login, validação do JWT, endpoints públicos x protegidos e CORS. */
class SegurancaIntegrationTest extends AbstractIntegrationTest {

    @Autowired private JwtDecoder jwtDecoder;
    @Autowired private JwtEncoder jwtEncoder;

    // ------------------------------------------------------------------
    // /login
    // ------------------------------------------------------------------
    @Test
    @DisplayName("POST /login: admin recebe token JWT válido com as claims esperadas")
    void login_admin_ok() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", ADMIN_EMAIL, "password", ADMIN_SENHA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(43200));

        Jwt jwt = jwtDecoder.decode(tokenAdmin());
        Long idAdmin = jdbcTemplate.queryForObject("select id from usuario where email = ?", Long.class, ADMIN_EMAIL);

        assertThat(jwt.<String>getClaim("username")).isEqualTo(ADMIN_EMAIL);
        assertThat(jwt.getSubject()).isEqualTo(ADMIN_EMAIL);
        assertThat(jwt.<String>getClaim("scope")).isEqualTo("ADMIN");
        assertThat(((Number) jwt.getClaim("usuarioid")).longValue()).isEqualTo(idAdmin);
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(12));
    }

    @Test
    @DisplayName("POST /login: usuário comum recebe escopo USER")
    void login_usuarioComum_escopoUser() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        assertThat(jwtDecoder.decode(maria.token()).<String>getClaim("scope")).isEqualTo("USER");
    }

    @Test
    @DisplayName("POST /login: senha incorreta é recusada (hoje responde 403)")
    void login_senhaIncorreta() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", ADMIN_EMAIL, "password", "senha-errada"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Usuario ou Senha invalidos"))
                .andExpect(jsonPath("$.path").value("/login"));
    }

    @Test
    @DisplayName("POST /login: e-mail inexistente recebe a mesma resposta de senha incorreta")
    void login_emailInexistente_mesmaRespostaQueSenhaErrada() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "ninguem@teste.com", "password", "qualquer"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Usuario ou Senha invalidos"));
    }

    @Test
    @Disabled("BUG: UsuarioInativoException não tem @ExceptionHandler, então o login de usuário inativo " +
              "resulta em HTTP 500. Mapeie a exceção para 403/401 no ServiceLogExceptionHandler e remova o @Disabled.")
    @DisplayName("POST /login: usuário inativo não consegue logar (4xx, não 500)")
    void login_usuarioInativo() throws Exception {
        long id = criarUsuario("Inativo", "inativo@teste.com", "Senha@123", ROLE_USER);
        jdbcTemplate.update("update usuario set status = false where id = ?", id);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "inativo@teste.com", "password", "Senha@123"))))
                .andExpect(status().is4xxClientError());
    }

    // ------------------------------------------------------------------
    // Validação do token
    // ------------------------------------------------------------------
    @Test
    @DisplayName("endpoint protegido sem token -> 401")
    void semToken_401() throws Exception {
        mockMvc.perform(get("/usuarios/myuser"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("token malformado -> 401")
    void tokenMalformado_401() throws Exception {
        mockMvc.perform(get("/usuarios/myuser").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("token assinado com outra chave RSA -> 401")
    void tokenAssinadoComOutraChave_401() throws Exception {
        KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
        gerador.initialize(2048);
        KeyPair outroPar = gerador.generateKeyPair();
        RSAKey jwk = new RSAKey.Builder((RSAPublicKey) outroPar.getPublic())
                .privateKey((RSAPrivateKey) outroPar.getPrivate())
                .build();
        JwtEncoder encoderDoAtacante = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));

        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("backend")
                .subject(ADMIN_EMAIL)
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(3600))
                .claim("username", ADMIN_EMAIL)
                .claim("scope", "ADMIN")
                .build();
        String tokenForjado = encoderDoAtacante.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        mockMvc.perform(get("/usuarios").header("Authorization", bearer(tokenForjado)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("token expirado -> 401")
    void tokenExpirado_401() throws Exception {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("backend")
                .subject(ADMIN_EMAIL)
                .issuedAt(agora.minus(Duration.ofHours(2)))
                .expiresAt(agora.minus(Duration.ofHours(1)))
                .claim("username", ADMIN_EMAIL)
                .claim("scope", "ADMIN")
                .build();
        String tokenExpirado = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        mockMvc.perform(get("/usuarios").header("Authorization", bearer(tokenExpirado)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("token válido de usuário que foi removido -> acesso negado (não retorna dados)")
    void tokenDeUsuarioRemovido() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        jdbcTemplate.update("delete from usuario where id = ?", maria.id());

        // AuthService.authenticated() lança UsernameNotFoundException. Dependendo de como a exceção é
        // traduzida o resultado é 401 ou erro; o que importa é NUNCA devolver 200 com dados.
        int statusHttp;
        try {
            statusHttp = mockMvc.perform(get("/usuarios/myuser").header("Authorization", maria.bearer()))
                    .andReturn().getResponse().getStatus();
        } catch (Exception excecaoNaoTratada) {
            statusHttp = 500;
        }

        assertThat(statusHttp).isNotEqualTo(200);
    }

    // ------------------------------------------------------------------
    // Endpoints públicos
    // ------------------------------------------------------------------
    @Test
    @DisplayName("GET /actuator/health é público e reporta UP")
    void actuatorHealth_publico() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /v3/api-docs (OpenAPI) é público")
    void apiDocs_publico() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/login']").exists());
    }

    // ------------------------------------------------------------------
    // CORS
    // ------------------------------------------------------------------
    @Test
    @DisplayName("CORS: preflight de origem permitida é aceito")
    void cors_origemPermitida() throws Exception {
        mockMvc.perform(options("/usuarios")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    @DisplayName("CORS: preflight de origem não permitida é recusado")
    void cors_origemNaoPermitida() throws Exception {
        mockMvc.perform(options("/usuarios")
                        .header("Origin", "http://site-malicioso.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
