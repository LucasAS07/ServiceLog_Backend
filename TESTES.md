# Testes do ServiceLog

## Como rodar

```bash
./mvnw test                                  # tudo (os de integração exigem Docker rodando)
./mvnw test -DexcludedGroups=integration     # só unitários (rápido, sem Docker)
./mvnw test -Dgroups=integration             # só integração
./mvnw test -Dtest=AtendimentoServiceTest    # uma classe
```

## Estrutura

| Pasta / classe | Tipo | O que cobre |
|---|---|---|
| `service/*Test` | Unitário (Mockito) | Regras de AtendimentoService, UsuarioService, TokenService, AuthService e EmailService |
| `mapper/AtendimentoMapperTest` | Unitário | Formatação e cálculos do MapStruct |
| `scheduler/EnvioFolhasAtendimentosTest` | Unitário | Envio mensal: período, falha isolada por usuário |
| `integration/AbstractIntegrationTest` | Base | PostgreSQL real (Testcontainers) + Flyway + MockMvc + helpers |
| `integration/BootstrapIntegrationTest` | Integração | Migrations, seeds de roles, admin criado na subida, diagnóstico do schema |
| `integration/SegurancaIntegrationTest` | Integração | Login, JWT (inválido/expirado/chave errada), endpoints públicos, CORS |
| `integration/UsuarioControllerIntegrationTest` | Integração | CRUD de usuários, perfis ADMIN x USER, validações |
| `integration/AtendimentoControllerIntegrationTest` | Integração | CRUD de atendimentos, filtros, paginação, total do dia, PDF |
| `integration/AtendimentoRepositoryIntegrationTest` | Integração | Queries JPQL (coalesce de datas, projeção, contagem) |

## Testes `@Disabled`

Marcados com `@Disabled("BUG: ...")`: descrevem o comportamento CORRETO e hoje falhariam.
Corrija o código e remova o `@Disabled` para que virem testes de regressão.

## Observações

- `src/test/resources/app.key` e `app.pub` são chaves RSA geradas só para teste.
- O perfil `test` (`application-test.properties`) define o admin dos testes (`admin@servicelog.test`).
- O `ServiceLogApplicationTests` original foi substituído: um `@SpringBootTest` puro falha sem banco e sem as chaves JWT.
