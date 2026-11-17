package io.lrsystem.ServiceLog;

import io.lrsystem.ServiceLog.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Smoke test: o contexto Spring sobe com PostgreSQL real, Flyway e chaves JWT de teste.
 * (Antes era um @SpringBootTest puro, que falhava sem banco, sem chaves e sem variáveis de ambiente.)
 */
class ServiceLogApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }

}
