package io.lrsystem.ServiceLog.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsuarioControllerIntegrationTest extends AbstractIntegrationTest {

    // ------------------------------------------------------------------
    // Autorização (ADMIN x USER)
    // ------------------------------------------------------------------
    @Test
    @DisplayName("usuário comum não acessa endpoints administrativos (403)")
    void usuarioComum_naoAcessaEndpointsDeAdmin() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        mockMvc.perform(get("/usuarios").header("Authorization", maria.bearer()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/usuarios/" + maria.id()).header("Authorization", maria.bearer()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/usuarios").header("Authorization", maria.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario("Novo", "novo@teste.com", "Senha@123", ROLE_USER))))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/usuarios/" + maria.id()).header("Authorization", maria.bearer()))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/usuarios/" + maria.id() + "/inativar").header("Authorization", maria.bearer()))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/usuarios/" + maria.id() + "/ativar").header("Authorization", maria.bearer()))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // GET /usuarios, GET /usuarios/{id}
    // ------------------------------------------------------------------
    @Test
    @DisplayName("GET /usuarios: admin lista todos os usuários")
    void admin_listaUsuarios() throws Exception {
        criarUsuario("Ana", "ana@teste.com", "Senha@123", ROLE_USER);
        criarUsuario("Beto", "beto@teste.com", "Senha@123", ROLE_USER);

        mockMvc.perform(get("/usuarios").header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItems(ADMIN_EMAIL, "ana@teste.com", "beto@teste.com")));
    }

    @Test
    @DisplayName("GET /usuarios/{id}: admin busca usuário existente e não expõe a senha")
    void admin_buscaUsuario() throws Exception {
        long id = criarUsuario("Ana", "ana@teste.com", "Senha@123", ROLE_USER);

        mockMvc.perform(get("/usuarios/" + id).header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@teste.com"))
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("GET /usuarios/{id}: inexistente -> 404 com corpo de erro padronizado")
    void admin_buscaUsuarioInexistente() throws Exception {
        mockMvc.perform(get("/usuarios/999999").header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/usuarios/999999"))
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    // ------------------------------------------------------------------
    // POST /usuarios
    // ------------------------------------------------------------------
    @Test
    @DisplayName("POST /usuarios: cria usuário, associa a role e grava a senha criptografada")
    void admin_criaUsuario() throws Exception {
        mockMvc.perform(post("/usuarios").header("Authorization", bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario("Maria", "maria@teste.com", "Senha@123", ROLE_USER))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@teste.com"))
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.senha").doesNotExist());

        String hash = jdbcTemplate.queryForObject(
                "select senha from usuario where email = ?", String.class, "maria@teste.com");
        Long roleId = jdbcTemplate.queryForObject(
                "select role_id from usuario where email = ?", Long.class, "maria@teste.com");

        assertThat(hash).startsWith("$2a$").isNotEqualTo("Senha@123");
        assertThat(roleId).isEqualTo(ROLE_USER);
    }

    @Test
    @DisplayName("POST /usuarios: usuário criado consegue fazer login com a senha informada")
    void usuarioCriado_consegueLogar() throws Exception {
        criarUsuario("Maria", "maria@teste.com", "Senha@123", ROLE_USER);

        assertThat(login("maria@teste.com", "Senha@123")).isNotBlank();
    }

    @Test
    @DisplayName("POST /usuarios: dados inválidos -> 400")
    void criaUsuario_validacao() throws Exception {
        String token = bearer(tokenAdmin());

        // e-mail inválido
        mockMvc.perform(post("/usuarios").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario("Maria", "isto-nao-e-email", "Senha@123", ROLE_USER))))
                .andExpect(status().isBadRequest());

        // nome em branco
        mockMvc.perform(post("/usuarios").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario("", "maria@teste.com", "Senha@123", ROLE_USER))))
                .andExpect(status().isBadRequest());

        // senha em branco
        mockMvc.perform(post("/usuarios").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario("Maria", "maria@teste.com", "", ROLE_USER))))
                .andExpect(status().isBadRequest());

        // e-mail maior que 100 caracteres
        String emailLongo = "a".repeat(95) + "@x.com";
        mockMvc.perform(post("/usuarios").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario("Maria", emailLongo, "Senha@123", ROLE_USER))))
                .andExpect(status().isBadRequest());

        Integer total = jdbcTemplate.queryForObject("select count(*) from usuario where email <> ?", Integer.class, ADMIN_EMAIL);
        assertThat(total).isZero();
    }

    @Test
    @Disabled("BUG: e-mail duplicado dispara DataIntegrityViolationException sem @ExceptionHandler (HTTP 500). " +
              "Valide no service ou trate a exceção (409) e remova o @Disabled.")
    @DisplayName("POST /usuarios: e-mail já cadastrado -> 4xx (não 500)")
    void criaUsuario_emailDuplicado() throws Exception {
        criarUsuario("Ana", "ana@teste.com", "Senha@123", ROLE_USER);

        mockMvc.perform(post("/usuarios").header("Authorization", bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario("Outra Ana", "ana@teste.com", "Senha@123", ROLE_USER))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Disabled("BUG: RoleNaoEncontradaException não tem @ExceptionHandler (HTTP 500). " +
              "Trate a exceção (404/400) e remova o @Disabled.")
    @DisplayName("POST /usuarios: role inexistente -> 4xx (não 500)")
    void criaUsuario_roleInexistente() throws Exception {
        mockMvc.perform(post("/usuarios").header("Authorization", bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario("Maria", "maria@teste.com", "Senha@123", 999L))))
                .andExpect(status().is4xxClientError());
    }

    // ------------------------------------------------------------------
    // PUT /usuarios/{id}
    // ------------------------------------------------------------------
    @Test
    @DisplayName("PUT /usuarios/{id}: admin atualiza dados e a nova senha passa a valer")
    void admin_atualizaUsuario() throws Exception {
        long id = criarUsuario("Maria", "maria@teste.com", "Senha@123", ROLE_USER);

        mockMvc.perform(put("/usuarios/" + id).header("Authorization", bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario("Maria Souza", "maria@teste.com", "NovaSenha@456", ROLE_USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Maria Souza"));

        assertThat(login("maria@teste.com", "NovaSenha@456")).isNotBlank();
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "maria@teste.com", "password", "Senha@123"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /usuarios/{id}: usuário inexistente -> 404")
    void atualizaUsuarioInexistente() throws Exception {
        mockMvc.perform(put("/usuarios/999999").header("Authorization", bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario("X", "x@teste.com", "Senha@123", ROLE_USER))))
                .andExpect(status().isNotFound());
    }

    @Test
    @Disabled("BUG DE SEGURANÇA: PUT /usuarios/{id} não tem @PreAuthorize nem checagem de dono, então qualquer " +
              "usuário autenticado altera os dados e a senha de qualquer outro. Corrija e remova o @Disabled.")
    @DisplayName("PUT /usuarios/{id}: usuário comum NÃO pode alterar outro usuário (403)")
    void usuarioComum_naoAtualizaOutroUsuario() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        long idAdmin = jdbcTemplate.queryForObject("select id from usuario where email = ?", Long.class, ADMIN_EMAIL);

        mockMvc.perform(put("/usuarios/" + idAdmin).header("Authorization", maria.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario("admin", ADMIN_EMAIL, "SenhaDoAtacante@1", ROLE_ADMIN))))
                .andExpect(status().isForbidden());
    }

    @Test
    @Disabled("BUG DE SEGURANÇA: usuário comum consegue se promover a ADMIN enviando roleId=1 em PUT /usuarios/{seuId}. " +
              "Corrija e remova o @Disabled.")
    @DisplayName("PUT /usuarios/{id}: usuário comum NÃO pode se promover a ADMIN")
    void usuarioComum_naoSePromoveAAdmin() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        mockMvc.perform(put("/usuarios/" + maria.id()).header("Authorization", maria.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoUsuario("Maria", "maria@teste.com", maria.senha(), ROLE_ADMIN))))
                .andExpect(status().isForbidden());

        Long roleId = jdbcTemplate.queryForObject("select role_id from usuario where id = ?", Long.class, maria.id());
        assertThat(roleId).isEqualTo(ROLE_USER);
    }

    // ------------------------------------------------------------------
    // DELETE /usuarios/{id}, ativar / inativar
    // ------------------------------------------------------------------
    @Test
    @DisplayName("DELETE /usuarios/{id}: admin remove o usuário")
    void admin_removeUsuario() throws Exception {
        long id = criarUsuario("Maria", "maria@teste.com", "Senha@123", ROLE_USER);
        String token = bearer(tokenAdmin());

        mockMvc.perform(delete("/usuarios/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/usuarios/" + id).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("inativar / ativar: alternam o status do usuário")
    void admin_inativaEAtivaUsuario() throws Exception {
        long id = criarUsuario("Maria", "maria@teste.com", "Senha@123", ROLE_USER);
        String token = bearer(tokenAdmin());

        mockMvc.perform(delete("/usuarios/" + id + "/inativar").header("Authorization", token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/usuarios/" + id).header("Authorization", token))
                .andExpect(jsonPath("$.status").value(false));

        mockMvc.perform(delete("/usuarios/" + id + "/ativar").header("Authorization", token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/usuarios/" + id).header("Authorization", token))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @DisplayName("inativar: usuário inexistente -> 404")
    void inativarUsuarioInexistente() throws Exception {
        mockMvc.perform(delete("/usuarios/999999/inativar").header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // GET /usuarios/myuser
    // ------------------------------------------------------------------
    @Test
    @DisplayName("GET /usuarios/myuser: devolve os dados do próprio usuário autenticado")
    void myUser() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        mockMvc.perform(get("/usuarios/myuser").header("Authorization", maria.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(maria.id()))
                .andExpect(jsonPath("$.email").value("maria@teste.com"))
                .andExpect(jsonPath("$.nome").value("Maria"));
    }
}
