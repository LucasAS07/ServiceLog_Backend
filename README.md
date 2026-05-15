# ServiceLog Backend

Backend da aplicação **ServiceLog**, desenvolvido para solucionar dificuldades reais do meu trabalho no dia a dia e também como forma de praticar e evoluir minhas habilidades em desenvolvimento de software.

O projeto está sendo construído utilizando Java com Spring Boot, focando em arquitetura limpa, boas práticas, segurança, organização de código e escalabilidade.

---

## 🚀 Objetivo do Projeto

O ServiceLog nasceu da necessidade de centralizar e organizar informações operacionais do ambiente de trabalho, permitindo registrar, consultar e gerenciar dados de forma mais eficiente.

Além do uso prático, o projeto também serve como laboratório de estudos para aprofundamento em:

- Desenvolvimento Backend com Java
- Spring Boot
- APIs REST
- Banco de dados relacionais
- Arquitetura de software
- Segurança e autenticação
- Boas práticas de desenvolvimento
- Integração entre sistemas

---

## 🛠️ Tecnologias Utilizadas

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- Maven
- JWT Authentication
- Lombok
- Hibernate

---

## 📁 Estrutura do Projeto

O projeto segue uma estrutura organizada em camadas:

```bash
src/main/java
 ├── controller
 ├── service
 ├── repository
 ├── entity
 ├── dto
 ├── config
 ├── security
 └── exception
```

---

## 🔐 Funcionalidades

- Autenticação com JWT
- Cadastro e gerenciamento de usuários
- Controle de permissões
- Registro de informações operacionais
- API RESTful
- Tratamento global de exceções
- Validações
- Integração com banco de dados PostgreSQL

---

## ⚙️ Como Executar o Projeto

### Pré-requisitos

- Java 17+ instalado
- Maven instalado
- PostgreSQL configurado

---

### 1. Clone o repositório

```bash
git clone https://github.com/LucasAS07/ServiceLog_Backend.git
```

---

### 2. Acesse o diretório

```bash
cd ServiceLog_Backend
```

---

### 3. Configure o banco de dados

Edite o arquivo:

```bash
src/main/resources/application.properties
```

Configure:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/nomedobanco
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

---

### 4. Execute o projeto

```bash
mvn spring-boot:run
```

---

## 📌 Futuras Melhorias

- Dashboard com métricas
- Relatórios em PDF
- Envio de e-mails automáticos
- Logs de auditoria
- Integração com sistemas externos
- Deploy em nuvem
- Documentação com Swagger/OpenAPI
- Testes automatizados

---

## 🎯 Motivação

Este projeto representa tanto uma solução prática para problemas reais enfrentados no trabalho quanto uma oportunidade de aprendizado contínuo.

A ideia é evoluir constantemente a aplicação, aplicando conceitos profissionais utilizados no mercado e adquirindo experiência prática no desenvolvimento de sistemas robustos.

---

## 📄 Licença

Este projeto está sob a licença MIT.

---

## 👨‍💻 Autor

Desenvolvido por Lucas Rodrigues

GitHub: https://github.com/LucasAS07
