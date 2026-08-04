# 🏨 Sistema de Reservas

Aplicação full-stack completa para gerenciamento de reservas de hospedagem, desenvolvida com **Spring Boot** no backend e **Angular** no frontend.

---

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Como Executar](#como-executar)
- [Endpoints da API](#endpoints-da-api)
- [Autenticação](#autenticação)
- [Testes](#testes)

---

## Sobre o Projeto

Sistema web para gerenciamento de reservas de hospedagem com operações completas de CRUD (Criar, Ler, Atualizar e Deletar). O sistema conta com uma interface moderna construída com Angular Material, autenticação no frontend e uma API REST robusta com tratamento global de erros.

---

## 🛠 Tecnologias Utilizadas

### Backend
| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | 25 | Linguagem principal |
| Spring Boot | 3.5.3 | Framework backend |
| Spring Data JPA | - | Persistência de dados |
| Spring Security | - | Segurança e CORS |
| Hibernate | 6.6 | ORM |
| PostgreSQL | 13 | Banco de dados relacional |
| Maven | 3.x | Gerenciador de dependências |
| Spring DevTools | - | Hot reload em desenvolvimento |
| JUnit 5 + Mockito | - | Testes unitários e de integração |

### Frontend
| Tecnologia | Versão | Finalidade |
|---|---|---|
| Angular | 19 | Framework frontend |
| Angular Material | 19 | Biblioteca de componentes UI |
| TypeScript | 5.x | Linguagem principal |
| RxJS | - | Programação reativa |
| SCSS | - | Estilização |

---

## ✅ Funcionalidades

### Gerenciamento de Reservas
- **Listar** todas as reservas em tabela com avatar do cliente, datas formatadas e chip de pessoas
- **Criar** nova reserva com formulário validado e datepicker
- **Editar** reserva existente com dados pré-preenchidos
- **Remover** reserva com dialog de confirmação Material (sem `confirm()` nativo)

### Interface do Usuário
- Design responsivo com **Angular Material** (tema M3)
- Toolbar fixa com navegação e botão de logout
- Animação de entrada (`fadeInUp`) nas páginas
- Avatar gerado a partir da inicial do nome do cliente
- Spinner de carregamento nas operações assíncronas
- Notificações de sucesso/erro com **MatSnackBar**
- Estado vazio estilizado quando não há reservas

### Autenticação (Frontend)
- Tela de login com fundo gradiente e card centralizado
- Toggle de visibilidade da senha
- Credenciais validadas no frontend com persistência via `localStorage`
- **AuthGuard** protegendo todas as rotas autenticadas
- Logout com limpeza de sessão e redirecionamento automático

### Qualidade e Testes
- 15 testes automatizados passando (JUnit 5 + Mockito + MockMvc)
- Testes de unidade para a camada de serviço
- Testes de integração para os controllers com MockMvc
- Perfil de teste separado usando banco H2 em memória
- Tratamento global de exceções com `@RestControllerAdvice`

---

## 🏗 Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                     FRONTEND (Angular)                  │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Login   │  │ Lista        │  │ Formulário       │  │
│  │Component │  │ Reservas     │  │ Reserva          │  │
│  └──────────┘  └──────────────┘  └──────────────────┘  │
│       │               │                  │              │
│  ┌────▼───────────────▼──────────────────▼────────┐     │
│  │         ReservaService (HttpClient)            │     │
│  └──────────────────────┬─────────────────────────┘     │
│                         │ HTTP                          │
│  ┌──────────────────────▼─────────────────────────┐     │
│  │         AuthService + AuthGuard                │     │
│  └────────────────────────────────────────────────┘     │
└─────────────────────────┼───────────────────────────────┘
                          │ REST API (porta 8080)
┌─────────────────────────▼───────────────────────────────┐
│                    BACKEND (Spring Boot)                 │
│  ┌─────────────────────────────────────────────────┐    │
│  │              ReservaController                  │    │
│  │         GET/POST/PUT/DELETE /api/reservas        │    │
│  └────────────────────┬────────────────────────────┘    │
│                       │                                  │
│  ┌────────────────────▼────────────────────────────┐    │
│  │              ReservaService                     │    │
│  │         (regras de negócio)                     │    │
│  └────────────────────┬────────────────────────────┘    │
│                       │                                  │
│  ┌────────────────────▼────────────────────────────┐    │
│  │           ReservaRepository (JPA)               │    │
│  └────────────────────┬────────────────────────────┘    │
│                       │                                  │
│  ┌────────────────────▼────────────────────────────┐    │
│  │          PostgreSQL (porta 5434)                │    │
│  │            banco: reservasdb                    │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

---

## 📁 Estrutura do Projeto

```
reservas/
├── reservas/                          # Backend Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/reservas/
│   │   │   │   ├── ReservasApplication.java
│   │   │   │   └── reserva/
│   │   │   │       ├── config/
│   │   │   │       │   ├── CorsConfig.java        # Configuração CORS
│   │   │   │       │   └── SecurityConfig.java    # Configuração Spring Security
│   │   │   │       ├── controller/
│   │   │   │       │   └── ReservaController.java # Endpoints REST
│   │   │   │       ├── dto/
│   │   │   │       │   └── ReservaDTO.java        # Data Transfer Objects
│   │   │   │       ├── exception/
│   │   │   │       │   └── GlobalExceptionHandler.java
│   │   │   │       ├── model/
│   │   │   │       │   └── Reserva.java           # Entidade JPA
│   │   │   │       ├── repository/
│   │   │   │       │   └── ReservaRepository.java # Repositório Spring Data
│   │   │   │       └── service/
│   │   │   │           └── ReservaService.java    # Lógica de negócio
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/com/reservas/
│   │           ├── ReservasApplicationTests.java
│   │           └── reserva/
│   │               ├── controller/ReservaControllerTest.java
│   │               └── service/ReservaServiceTest.java
│   └── pom.xml
│
└── reservas-frontend/                 # Frontend Angular
    ├── src/
    │   └── app/
    │       ├── components/
    │       │   ├── confirm-dialog/    # Dialog de confirmação reutilizável
    │       │   ├── login/             # Tela de login
    │       │   ├── reserva-form/      # Formulário criar/editar reserva
    │       │   └── reserva-lista/     # Listagem de reservas
    │       ├── guards/
    │       │   └── auth.guard.ts      # Proteção de rotas
    │       ├── models/
    │       │   └── reserva.model.ts   # Interface TypeScript
    │       ├── services/
    │       │   ├── auth.service.ts    # Gerenciamento de autenticação
    │       │   └── reserva.service.ts # Comunicação com a API
    │       ├── app.routes.ts          # Definição de rotas
    │       └── app.config.ts          # Configuração da aplicação
    └── package.json
```

---

## 💻 Pré-requisitos

- **Java 25** (JDK)
- **Maven 3.6+**
- **Node.js 18+** e **npm**
- **PostgreSQL 13+** rodando na porta `5434`
- **Angular CLI** (`npm install -g @angular/cli`)

### Configuração do Banco de Dados

Crie o banco de dados no PostgreSQL:
```sql
CREATE DATABASE reservasdb;
```

Configuração padrão em `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5434/reservasdb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
```

---

## ▶ Como Executar

### Backend (Spring Boot)

```bash
cd reservas
mvn spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

### Frontend (Angular)

```bash
cd reservas-frontend
npm install
ng serve
```

A aplicação estará disponível em: `http://localhost:4200`

---

## 📡 Endpoints da API

Base URL: `http://localhost:8080/api/reservas`

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/reservas` | Lista todas as reservas |
| `GET` | `/api/reservas/{id}` | Busca reserva por ID |
| `POST` | `/api/reservas` | Cria nova reserva |
| `PUT` | `/api/reservas/{id}` | Atualiza reserva existente |
| `DELETE` | `/api/reservas/{id}` | Remove uma reserva |

### Exemplo de Payload (POST/PUT)

```json
{
  "nomeCliente": "João Silva",
  "emailCliente": "joao@email.com",
  "dataCheckIn": "2026-09-01",
  "dataCheckOut": "2026-09-05",
  "quantidadePessoas": 2,
  "observacoes": "Quarto com vista para o mar"
}
```

---

## 🔐 Autenticação

A autenticação é gerenciada no frontend. As credenciais disponíveis são:

| Usuário | Senha |
|---|---|
| `admin` | `admin123` |
| `recepcao` | `recepcao123` |

A sessão é mantida via `localStorage`. Ao fazer login, o usuário é redirecionado para a lista de reservas. Rotas protegidas pelo `AuthGuard` redirecionam para `/login` caso não haja sessão ativa.

---

## 🧪 Testes

Para executar os testes do backend:

```bash
cd reservas
mvn test
```

**15 testes passando**, cobrindo:
- `ReservasApplicationTests` — carregamento do contexto Spring
- `ReservaServiceTest` — testes unitários com Mockito (listar, buscar, criar, atualizar, remover)
- `ReservaControllerTest` — testes de integração com MockMvc

O perfil de teste utiliza banco **H2 em memória**, configurado em `src/test/resources/application-test.properties`.

---

## 👨‍💻 Autor

Desenvolvido por **Sérgio** como projeto de estudo full-stack com Spring Boot e Angular.
