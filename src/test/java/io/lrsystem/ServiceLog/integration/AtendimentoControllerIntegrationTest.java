package io.lrsystem.ServiceLog.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AtendimentoControllerIntegrationTest extends AbstractIntegrationTest {

    // ------------------------------------------------------------------
    // POST /atendimentos
    // ------------------------------------------------------------------
    @Test
    @DisplayName("POST /atendimentos: sem token -> 401")
    void criar_semToken() throws Exception {
        mockMvc.perform(post("/atendimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoAtendimento("2026-01-28", "18:10", "18:22", "x"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /atendimentos: cria o atendimento e calcula colaborador, dia da semana e tempo total")
    void criar_ok() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        MvcResult resultado = mockMvc.perform(post("/atendimentos")
                        .header("Authorization", maria.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoAtendimento("2026-01-28", "18:10", "18:22", "Redefinição de senha"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.colaborador").value("Maria"))
                .andExpect(jsonPath("$.dia").value("28/01"))
                .andExpect(jsonPath("$.diaDaSemana").value("quarta-feira"))
                .andExpect(jsonPath("$.inicio").value("2026-01-28T18:10"))
                .andExpect(jsonPath("$.fim").value("2026-01-28T18:22"))
                .andExpect(jsonPath("$.tempoTotal").value("00:12"))
                .andReturn();

        long id = lerId(resultado);
        Long donoId = jdbcTemplate.queryForObject("select id_usuario from atendimento where id = ?", Long.class, id);
        assertThat(donoId).isEqualTo(maria.id());
    }

    @Test
    @DisplayName("POST /atendimentos: atendimento que atravessa a meia-noite termina no dia seguinte")
    void criar_viradaDeDia() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        mockMvc.perform(post("/atendimentos")
                        .header("Authorization", maria.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoAtendimento("2026-01-28", "23:50", "00:10", "Plantão"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inicio").value("2026-01-28T23:50"))
                .andExpect(jsonPath("$.fim").value("2026-01-29T00:10"))
                .andExpect(jsonPath("$.tempoTotal").value("00:20"));
    }

    @Test
    @DisplayName("POST /atendimentos: campos obrigatórios ausentes -> 400")
    void criar_camposObrigatorios() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        mockMvc.perform(post("/atendimentos")
                        .header("Authorization", maria.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /atendimentos: horário em formato inválido -> 400 com mensagem orientando HH:mm")
    void criar_horarioInvalido() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        mockMvc.perform(post("/atendimentos")
                        .header("Authorization", maria.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoAtendimento("2026-01-28", "25:99", "18:22", "x"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error", containsString("HH:mm")));
    }

    // ------------------------------------------------------------------
    // GET /usuarios/atendimentos
    // ------------------------------------------------------------------
    @Test
    @DisplayName("GET /usuarios/atendimentos: lista apenas os atendimentos do usuário autenticado, ordenados por dia")
    void listar_somenteDoUsuario() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        Sessao joao = criarUsuarioComumLogado("João", "joao@teste.com");
        criarAtendimento(maria.token(), "2026-01-20", "10:00", "11:00", "B");
        criarAtendimento(maria.token(), "2026-01-10", "09:00", "09:30", "A");
        criarAtendimento(joao.token(), "2026-01-15", "14:00", "15:00", "do João");

        mockMvc.perform(get("/usuarios/atendimentos").header("Authorization", maria.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].dia").value("10/01"))
                .andExpect(jsonPath("$.content[1].dia").value("20/01"))
                .andExpect(jsonPath("$.content[0].colaborador").value("Maria"));
    }

    @Test
    @DisplayName("GET /usuarios/atendimentos: filtra por período (limites inclusivos)")
    void listar_filtroPorPeriodo() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        criarAtendimento(maria.token(), "2026-01-10", "09:00", "09:30", "A");
        criarAtendimento(maria.token(), "2026-01-20", "10:00", "11:00", "B");

        // só início
        mockMvc.perform(get("/usuarios/atendimentos").param("inicio", "2026-01-15")
                        .header("Authorization", maria.bearer()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].dia").value("20/01"));

        // só fim
        mockMvc.perform(get("/usuarios/atendimentos").param("fim", "2026-01-15")
                        .header("Authorization", maria.bearer()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].dia").value("10/01"));

        // início e fim exatamente nos dias dos registros
        mockMvc.perform(get("/usuarios/atendimentos")
                        .param("inicio", "2026-01-10").param("fim", "2026-01-20")
                        .header("Authorization", maria.bearer()))
                .andExpect(jsonPath("$.totalElements").value(2));

        // período sem registros
        mockMvc.perform(get("/usuarios/atendimentos")
                        .param("inicio", "2026-02-01").param("fim", "2026-02-28")
                        .header("Authorization", maria.bearer()))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /usuarios/atendimentos: fim anterior ao início -> 400")
    void listar_periodoInvalido() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        mockMvc.perform(get("/usuarios/atendimentos")
                        .param("inicio", "2026-02-10").param("fim", "2026-02-01")
                        .header("Authorization", maria.bearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Data fim")));
    }

    @Test
    @DisplayName("GET /usuarios/atendimentos: paginação")
    void listar_paginacao() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        criarAtendimento(maria.token(), "2026-01-10", "09:00", "09:30", "A");
        criarAtendimento(maria.token(), "2026-01-11", "09:00", "09:30", "B");
        criarAtendimento(maria.token(), "2026-01-12", "09:00", "09:30", "C");

        mockMvc.perform(get("/usuarios/atendimentos").param("size", "2").param("page", "1")
                        .header("Authorization", maria.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].dia").value("12/01"));
    }

    // ------------------------------------------------------------------
    // PUT /atendimentos/{id}
    // ------------------------------------------------------------------
    @Test
    @DisplayName("PUT /atendimentos/{id}: atualiza horários, tempo total e justificativa (hoje responde 201)")
    void atualizar_ok() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        long id = criarAtendimento(maria.token(), "2026-01-28", "18:10", "18:22", "Antes");

        mockMvc.perform(put("/atendimentos/" + id)
                        .header("Authorization", maria.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoAtendimento("2026-01-28", "08:00", "09:30", "Depois"))))
                .andExpect(status().isCreated()) // comportamento atual; REST recomendaria 200
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.inicio").value("2026-01-28T08:00"))
                .andExpect(jsonPath("$.fim").value("2026-01-28T09:30"))
                .andExpect(jsonPath("$.tempoTotal").value("01:30"))
                .andExpect(jsonPath("$.justificativa").value("Depois"));

        String justificativa = jdbcTemplate.queryForObject(
                "select justificativa from atendimento where id = ?", String.class, id);
        assertThat(justificativa).isEqualTo("Depois");
    }

    @Test
    @DisplayName("PUT /atendimentos/{id}: inexistente -> 404")
    void atualizar_naoEncontrado() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        mockMvc.perform(put("/atendimentos/999999")
                        .header("Authorization", maria.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoAtendimento("2026-01-28", "08:00", "09:30", "x"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /atendimentos/{id}: corpo inválido -> 400")
    void atualizar_validacao() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        long id = criarAtendimento(maria.token(), "2026-01-28", "18:10", "18:22", "x");

        Map<String, Object> semHorarios = new LinkedHashMap<>();
        semHorarios.put("dia", "2026-01-28");

        mockMvc.perform(put("/atendimentos/" + id)
                        .header("Authorization", maria.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(semHorarios)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Disabled("BUG: AtendimentoService.atualizar() usa Duration.between(LocalTime, LocalTime); na virada de dia " +
              "o tempo total fica negativo. Use os LocalDateTime (como em adicionar) e remova o @Disabled.")
    @DisplayName("PUT /atendimentos/{id}: atendimento que vira a meia-noite tem tempo total positivo")
    void atualizar_viradaDeDia() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        long id = criarAtendimento(maria.token(), "2026-01-28", "18:10", "18:22", "x");

        mockMvc.perform(put("/atendimentos/" + id)
                        .header("Authorization", maria.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoAtendimento("2026-01-28", "23:50", "00:10", "x"))))
                .andExpect(jsonPath("$.fim").value("2026-01-29T00:10"))
                .andExpect(jsonPath("$.tempoTotal").value("00:20"));
    }

    @Test
    @Disabled("BUG: ao mudar o 'dia' em PUT, 'diaDaSemana' não é recalculado e fica desatualizado.")
    @DisplayName("PUT /atendimentos/{id}: mudar o dia recalcula o dia da semana")
    void atualizar_recalculaDiaDaSemana() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        long id = criarAtendimento(maria.token(), "2026-01-28", "18:10", "18:22", "x"); // quarta

        mockMvc.perform(put("/atendimentos/" + id)
                        .header("Authorization", maria.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoAtendimento("2026-01-30", "18:10", "18:22", "x")))) // sexta
                .andExpect(jsonPath("$.diaDaSemana").value("sexta-feira"));
    }

    @Test
    @Disabled("BUG DE SEGURANÇA: PUT /atendimentos/{id} não verifica o dono; qualquer usuário autenticado altera " +
              "atendimentos de outros. Use authService.validateSelfOrAdmin (como em deletar) e remova o @Disabled.")
    @DisplayName("PUT /atendimentos/{id}: usuário comum NÃO pode alterar atendimento de outro usuário (403)")
    void atualizar_deOutroUsuario() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        Sessao joao = criarUsuarioComumLogado("João", "joao@teste.com");
        long idDaMaria = criarAtendimento(maria.token(), "2026-01-28", "18:10", "18:22", "da Maria");

        mockMvc.perform(put("/atendimentos/" + idDaMaria)
                        .header("Authorization", joao.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpoAtendimento("2026-01-28", "08:00", "09:00", "alterado pelo João"))))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // DELETE /atendimentos/{id}
    // ------------------------------------------------------------------
    @Test
    @DisplayName("DELETE /atendimentos/{id}: dono remove o próprio atendimento")
    void deletar_proprio() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        long id = criarAtendimento(maria.token(), "2026-01-28", "18:10", "18:22", "x");

        mockMvc.perform(delete("/atendimentos/" + id).header("Authorization", maria.bearer()))
                .andExpect(status().isNoContent());

        Integer restantes = jdbcTemplate.queryForObject(
                "select count(*) from atendimento where id = ?", Integer.class, id);
        assertThat(restantes).isZero();
    }

    @Test
    @DisplayName("DELETE /atendimentos/{id}: admin remove atendimento de outro usuário")
    void deletar_adminRemoveDeOutro() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        long id = criarAtendimento(maria.token(), "2026-01-28", "18:10", "18:22", "x");

        mockMvc.perform(delete("/atendimentos/" + id).header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isNoContent());

        Integer restantes = jdbcTemplate.queryForObject(
                "select count(*) from atendimento where id = ?", Integer.class, id);
        assertThat(restantes).isZero();
    }

    @Test
    @DisplayName("DELETE /atendimentos/{id}: inexistente -> 404")
    void deletar_naoEncontrado() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        mockMvc.perform(delete("/atendimentos/999999").header("Authorization", maria.bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @Disabled("BUG: ForbiddenException (lançada em AuthService.validateSelfOrAdmin) não tem @ExceptionHandler, " +
              "então apagar atendimento de outro usuário resulta em HTTP 500 em vez de 403. O atendimento NÃO é " +
              "apagado (a regra funciona), mas a resposta está errada. Trate a exceção e remova o @Disabled.")
    @DisplayName("DELETE /atendimentos/{id}: usuário comum não remove atendimento de outro -> 403")
    void deletar_deOutroUsuario() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        Sessao joao = criarUsuarioComumLogado("João", "joao@teste.com");
        long idDaMaria = criarAtendimento(maria.token(), "2026-01-28", "18:10", "18:22", "da Maria");

        mockMvc.perform(delete("/atendimentos/" + idDaMaria).header("Authorization", joao.bearer()))
                .andExpect(status().isForbidden());

        Integer restantes = jdbcTemplate.queryForObject(
                "select count(*) from atendimento where id = ?", Integer.class, idDaMaria);
        assertThat(restantes).isEqualTo(1);
    }

    // ------------------------------------------------------------------
    // GET /atendimentos/total-dia
    // ------------------------------------------------------------------
    @Test
    @DisplayName("GET /atendimentos/total-dia: conta só os atendimentos de hoje do usuário autenticado")
    void totalDia() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        Sessao joao = criarUsuarioComumLogado("João", "joao@teste.com");
        String hoje = LocalDate.now().toString();

        criarAtendimento(maria.token(), hoje, "09:00", "09:30", "1");
        criarAtendimento(maria.token(), hoje, "10:00", "10:30", "2");
        criarAtendimento(maria.token(), LocalDate.now().minusDays(1).toString(), "10:00", "10:30", "ontem");
        criarAtendimento(joao.token(), hoje, "10:00", "10:30", "do João");

        mockMvc.perform(get("/atendimentos/total-dia").header("Authorization", maria.bearer()))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    @DisplayName("GET /atendimentos/total-dia: sem atendimentos retorna 0")
    void totalDia_zero() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        mockMvc.perform(get("/atendimentos/total-dia").header("Authorization", maria.bearer()))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    // ------------------------------------------------------------------
    // GET /atendimentos/relatorios (PDF)
    // ------------------------------------------------------------------
    @Test
    @DisplayName("GET /atendimentos/relatorios: gera a folha de atendimentos em PDF")
    void relatorio_pdf() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");
        criarAtendimento(maria.token(), "2026-01-10", "09:00", "09:30", "A");
        criarAtendimento(maria.token(), "2026-01-20", "10:00", "11:00", "B");

        MvcResult resultado = mockMvc.perform(get("/atendimentos/relatorios")
                        .param("inicio", "2026-01-01").param("fim", "2026-01-31")
                        .header("Authorization", maria.bearer()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();

        byte[] pdf = resultado.getResponse().getContentAsByteArray();
        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("GET /atendimentos/relatorios: parâmetros obrigatórios ausentes -> 400")
    void relatorio_semParametros() throws Exception {
        Sessao maria = criarUsuarioComumLogado("Maria", "maria@teste.com");

        mockMvc.perform(get("/atendimentos/relatorios").header("Authorization", maria.bearer()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /atendimentos/relatorios: sem token -> 401")
    void relatorio_semToken() throws Exception {
        mockMvc.perform(get("/atendimentos/relatorios")
                        .param("inicio", "2026-01-01").param("fim", "2026-01-31"))
                .andExpect(status().isUnauthorized());
    }
}
