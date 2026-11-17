package io.lrsystem.ServiceLog.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base dos testes de integração.
 *
 * - Sobe UM PostgreSQL real (Testcontainers) compartilhado por todas as classes de teste.
 *   As migrations do Flyway rodam de verdade, então o schema testado é o mesmo da produção.
 * - Usa o perfil "test" (src/test/resources/application-test.properties), que traz as chaves JWT de teste
 *   e as credenciais do admin criado pelo AdminUserConfig.
 * - Antes de cada teste o banco é limpo (exceto o usuário admin).
 *
 * Requer Docker rodando. Para rodar só os testes unitários: ./mvnw test -DexcludedGroups=integration
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    // Devem bater com user.email / user.senha de application-test.properties
    protected static final String ADMIN_EMAIL = "admin@servicelog.test";
    protected static final String ADMIN_SENHA = "Admin@123";

    // Seeds da migration V3
    protected static final long ROLE_ADMIN = 1L;
    protected static final long ROLE_USER = 2L;

    @SuppressWarnings("resource") // encerrado pelo Ryuk do Testcontainers ao fim da JVM
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    protected static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    protected void limparBanco() {
        jdbcTemplate.update("delete from atendimento");
        jdbcTemplate.update("delete from usuario where email <> ?", ADMIN_EMAIL);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Usuário criado via API e já autenticado. */
    protected record Sessao(long id, String nome, String email, String senha, String token) {
        public String bearer() {
            return "Bearer " + token;
        }
    }

    protected String json(Object objeto) throws Exception {
        return objectMapper.writeValueAsString(objeto);
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected long lerId(MvcResult resultado) throws Exception {
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
    }

    /** Faz login pelo endpoint real e devolve o access token. */
    protected String login(String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", senha))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("accessToken").asText();
    }

    protected String tokenAdmin() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_SENHA);
    }

    protected Map<String, Object> corpoUsuario(String nome, String email, String senha, long roleId) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("nome", nome);
        corpo.put("email", email);
        corpo.put("senha", senha);
        corpo.put("status", true);
        corpo.put("roleId", roleId);
        return corpo;
    }

    protected Map<String, Object> corpoAtendimento(String dia, String inicio, String fim, String justificativa) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("dia", dia);
        corpo.put("inicio", inicio);
        corpo.put("fim", fim);
        corpo.put("justificativa", justificativa);
        return corpo;
    }

    /** Cria um usuário (via POST /usuarios como admin) e devolve o id. */
    protected long criarUsuario(String nome, String email, String senha, long roleId) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/usuarios")
                        .header("Authorization", bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario(nome, email, senha, roleId))))
                .andExpect(status().isCreated())
                .andReturn();
        return lerId(resultado);
    }

    /** Cria um usuário com role USER e já faz login com ele. */
    protected Sessao criarUsuarioComumLogado(String nome, String email) throws Exception {
        String senha = "Senha@123";
        long id = criarUsuario(nome, email, senha, ROLE_USER);
        return new Sessao(id, nome, email, senha, login(email, senha));
    }

    /** Cria um atendimento (via POST /atendimentos) com o token informado e devolve o id. */
    protected long criarAtendimento(String token, String dia, String inicio, String fim, String justificativa)
            throws Exception {
        MvcResult resultado = mockMvc.perform(post("/atendimentos")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoAtendimento(dia, inicio, fim, justificativa))))
                .andExpect(status().isCreated())
                .andReturn();
        return lerId(resultado);
    }
}
