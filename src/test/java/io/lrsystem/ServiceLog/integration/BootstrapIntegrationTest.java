package io.lrsystem.ServiceLog.integration;

import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.RoleRepository;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica o que deve existir logo após a subida da aplicação: migrations, seeds e usuário admin. */
class BootstrapIntegrationTest extends AbstractIntegrationTest {

    @Autowired private RoleRepository roleRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Flyway aplicou todas as migrations com sucesso")
    void migrationsAplicadas() {
        Integer sucesso = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success", Integer.class);
        Integer falhas = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where not success", Integer.class);

        assertThat(sucesso).isGreaterThanOrEqualTo(5);
        assertThat(falhas).isZero();
    }

    @Test
    @DisplayName("roles ADMIN (1) e USER (2) existem (seed da V3)")
    void rolesSeed() {
        assertThat(roleRepository.findByNome("ADMIN").getId()).isEqualTo(ROLE_ADMIN);
        assertThat(roleRepository.findByNome("USER").getId()).isEqualTo(ROLE_USER);
    }

    @Test
    @DisplayName("AdminUserConfig cria o admin ativo, com role ADMIN e senha criptografada")
    void adminCriadoNaSubida() {
        Usuario admin = usuarioRepository.findByEmail(ADMIN_EMAIL).orElseThrow();

        assertThat(admin.getNome()).isEqualTo("admin");
        assertThat(admin.isStatus()).isTrue();
        assertThat(admin.getRole().getNome()).isEqualTo("ADMIN");
        assertThat(admin.getSenha()).isNotEqualTo(ADMIN_SENHA);
        assertThat(passwordEncoder.matches(ADMIN_SENHA, admin.getSenha())).isTrue();
    }

    @Test
    @DisplayName("schema: colunas legadas inicio/fim (TIME) da tabela atendimento não podem bloquear o INSERT da entidade")
    void schema_colunasLegadasNaoBloqueiamInsert() {
        // A entidade Atendimento mapeia inicio_ts / fim_ts. As colunas originais da V1 (inicio, fim) são
        // NOT NULL e, se continuarem assim, todo INSERT feito pela aplicação falha com violação de not-null.
        List<String> obrigatoriasLegadas = jdbcTemplate.queryForList("""
                select column_name
                from information_schema.columns
                where table_name = 'atendimento'
                  and column_name in ('inicio', 'fim')
                  and is_nullable = 'NO'
                  and column_default is null
                """, String.class);

        assertThat(obrigatoriasLegadas)
                .as("colunas NOT NULL sem default que a entidade não preenche (crie uma migration V6 para removê-las ou torná-las opcionais)")
                .isEmpty();
    }
}
