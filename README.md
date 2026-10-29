# PomoHistória

O **PomoHistória** é uma plataforma de estudo de História que junta a **técnica Pomodoro** a uma **jornada de questionários por módulos**. O estudante alterna sessões de foco e descanso com um temporizador e avança por etapas de perguntas. Cada etapa só é liberada depois que a anterior é aprovada.

## Sumário

- [Funcionalidades](#funcionalidades)
- [Como a jornada funciona](#como-a-jornada-funciona)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Estrutura do repositório](#estrutura-do-repositório)
- [Rodando localmente](#rodando-localmente)
- [Testes](#testes)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Segurança](#segurança)
- [Deploy](#deploy)
- [Fluxo de trabalho com Git](#fluxo-de-trabalho-com-git)
- [Documentação complementar](#documentação-complementar)
- [Autoria](#autoria)

## Funcionalidades

**Para o estudante**
- Cadastro e login com sessão segura.
- Dois temporizadores Pomodoro, um de foco e um de descanso, com duração ajustável. Eles continuam contando ao trocar de página.
- Questionário dividido em módulos e partes. A correção é feita no servidor e o progresso fica salvo por usuário.
- Fundo da tela inicial que muda conforme o módulo que o estudante já abriu.
- Perfil editável: nome, telefone, e-mail, senha e foto.

**Para o administrador**
- Tela para editar o enunciado, as alternativas e a resposta correta de cada questão.

## Como a jornada funciona

| Módulo | Tema | Partes | Questões por parte |
|---|---|---|---|
| 1 | Antiguidade Clássica | 3 | 4 |
| 2 | Era Vargas | 3 | 4 |

- **Ordem:** o estudante começa no Módulo 1, Parte 1.
- **Aprovação:** cada parte tem 4 questões de múltipla escolha (A a E) e exige pelo menos **70% de acertos**, ou seja, 3 de 4. Ao ser aprovado, a parte seguinte é liberada; ao concluir as 3 partes do Módulo 1, o Módulo 2 é liberado.
- **Gabarito:** a correção acontece só no backend. As respostas corretas nunca são enviadas ao navegador do estudante.
- **Fundo da tela:** o Início e o Temporizador mostram o fundo inicial até o estudante abrir o primeiro módulo. Depois, mostram a Grécia (Módulo 1) e, a partir do Módulo 2, a Era Vargas.

As 24 questões iniciais são carregadas automaticamente de [`backend/src/main/resources/db/seed/questionario/`](backend/src/main/resources/db/seed/questionario) na primeira vez que o backend sobe com o banco vazio.

## Tecnologias

| Camada | Tecnologias |
|---|---|
| **Frontend** | React 19, TypeScript, Vite 8, React Router 7, CSS Modules |
| **Backend** | Java 25, Spring Boot 4 (Web MVC, Security, Data JPA, Validation), Hibernate |
| **Banco de dados** | PostgreSQL, com migrações versionadas pelo Flyway |
| **Testes** | JUnit 5, Mockito, MockMvc, PostgreSQL embutido (zonky) |
| **Infraestrutura** | Docker (backend) e Render (hospedagem) |

## Arquitetura

```
┌───────────────────────┐   HTTPS + cookie de sessão   ┌─────────────────────────┐        ┌────────────┐
│ Frontend (React/Vite) │ ───────────────────────────▶ │ Backend (Spring Boot)   │ ─────▶ │ PostgreSQL │
│ site estático         │ ◀─────────────────────────── │ API REST em /api/*      │        │ (Flyway)   │
└───────────────────────┘    JSON  +  token CSRF       └─────────────────────────┘        └────────────┘
```

- **Comunicação:** o frontend é um site estático que conversa com a API REST. A autenticação usa **sessão HTTP** (cookie `JSESSIONID`, `HttpOnly`).
- **CSRF:** toda requisição que altera dados envia um **token CSRF** no header `X-XSRF-TOKEN`.
- **Banco:** o esquema é criado e atualizado pelo **Flyway** (`db/migration`); o Hibernate só valida que as entidades batem com as tabelas.
- **Organização do backend:** por funcionalidade (`auth`, `usuario`, `questionario`, `security`, `common`), cada uma com o próprio controller, serviço, repositório e DTOs.

## Estrutura do repositório

```
PomoHistoria/
├── backend/                         API Spring Boot
│   ├── src/main/java/.../
│   │   ├── auth/                    cadastro, login, CSRF e limite de tentativas
│   │   ├── usuario/                 conta, perfil, foto e criação do admin
│   │   ├── questionario/            questões, progresso, módulos e edição pelo admin
│   │   ├── security/                Spring Security, CORS e sessão
│   │   └── common/                  tratamento de erros e health check
│   ├── src/main/resources/
│   │   ├── application.yaml         configuração (produção por padrão)
│   │   ├── application-local.yaml   ajustes para rodar na sua máquina
│   │   └── db/                      migrações Flyway e seed das questões
│   ├── src/test/                    testes unitários e de integração
│   ├── Dockerfile
│   ├── .env.example                 modelo das variáveis de ambiente
│   ├── README.md                    como rodar o backend
│   └── DEPLOY.md                    deploy no Render e lista de rotas da API
│
└── frontend/pomohistoria/           aplicação React
    ├── public/                      favicon e ícone para celular
    ├── src/
    │   ├── components/              páginas (Início, Temporizador, Questionário, Perfil, Admin) e formulários
    │   ├── hooks/                   sessão do usuário, temporizadores e fundo por módulo
    │   ├── routes/                  rotas e proteção por login/papel
    │   ├── services/                chamadas à API (cliente HTTP compartilhado)
    │   └── assets/                  imagens (.webp)
    ├── .env.development / .env.production / .env.example
    └── README.md                    detalhes do frontend
```

## Rodando localmente

### Pré-requisitos

- **Java 25**
- **Node.js 22.12 ou mais recente**
- **PostgreSQL** instalado e rodando (versão 14 ou superior)

### 1. Banco de dados

No `psql` ou no SQL Shell do PostgreSQL:

```sql
CREATE DATABASE pomohistoria;
```

### 2. Backend

```powershell
cd backend
```

Copie `.env.example` para `.env` (se ainda não existir) e preencha pelo menos `SPRING_DATASOURCE_PASSWORD` com a senha do seu PostgreSQL. Opcionalmente, preencha `ADMIN_EMAIL` e `ADMIN_PASSWORD` para criar uma conta de administrador.

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Em Linux ou macOS, use `./mvnw` no lugar de `.\mvnw.cmd`.

A API sobe em `http://localhost:8080`. Para conferir, abra `http://localhost:8080/api/health`, que deve responder `{"status":"ok"}`. Na primeira execução, o Flyway cria as tabelas e as questões são carregadas.

O perfil `local` é obrigatório na sua máquina: ele lê o arquivo `.env` e ajusta o cookie de sessão para funcionar em `http://localhost`, sem HTTPS.

### 3. Frontend

Em outro terminal:

```powershell
cd frontend/pomohistoria
npm install
npm run dev
```

Abra `http://localhost:5173`. O arquivo `.env.development` já aponta o frontend para o backend local.

### 4. Usando

1. Crie uma conta em **Cadastre-se** e faça login.
2. Use os temporizadores no **Início** ou na página **Pomodoro**.
3. Abra o **Questionário** e avance pelas partes.
4. Para editar questões, entre com a conta admin e use **Perfil → Administração**.

## Testes

**Backend:** são 40 testes, entre unitários e de integração. Eles sobem um PostgreSQL embutido automaticamente, então não precisam de banco local nem do `.env`.

```powershell
cd backend
.\mvnw.cmd test
```

Os testes de integração cobrem os fluxos reais da API:
- cadastro, login, sessão e CSRF;
- permissões de usuário e de admin;
- bloqueio por tentativas de login;
- perfil e foto;
- edição de questões e progresso do questionário.

**Frontend:** checagem de tipos, lint e build de produção.

```powershell
cd frontend/pomohistoria
npm run lint
npm run build
```

## Variáveis de ambiente

As variáveis principais estão abaixo. A lista completa, comentada, está em [`backend/.env.example`](backend/.env.example) e [`frontend/pomohistoria/.env.example`](frontend/pomohistoria/.env.example).

| Onde | Variável | Para quê |
|---|---|---|
| Backend | `SPRING_DATASOURCE_URL` | Endereço do banco (`jdbc:postgresql://host:5432/banco`) |
| Backend | `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Credenciais do banco |
| Backend | `CORS_ALLOWED_ORIGINS` | URL(s) do frontend autorizadas a chamar a API |
| Backend | `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Opcionais: criam a conta admin na inicialização |
| Frontend | `VITE_API_BASE_URL` | URL pública do backend |

- O `backend/.env` serve só para desenvolvimento e **não vai para o git**.
- Em produção, as variáveis são cadastradas no painel da hospedagem.
- Variáveis `VITE_*` ficam visíveis no JavaScript do site, então **nunca coloque segredos nelas**.

## Segurança

- **Senhas:** guardadas com **BCrypt**; o cadastro exige no mínimo 8 caracteres.
- **Sessão:** o ID da sessão muda no login. O cookie é `HttpOnly` e `Secure`, e o `SameSite` é configurável.
- **Papel e status:** conferidos no banco a cada requisição. Desativar ou rebaixar um usuário vale na hora.
- **CSRF:** obrigatório em toda requisição que altera dados.
- **CORS:** restrito às origens configuradas.
- **Tentativas de login:** limitadas por e-mail e por IP. As respostas não revelam se um e-mail está cadastrado.
- **Dados sensíveis:** trocar e-mail ou senha exige a senha atual. O gabarito só sai pelas rotas de admin.
- **Container:** o backend roda sem privilégios de root.

## Deploy

O projeto foi preparado para o **Render**:

- **Backend:** Web Service do tipo Docker, com diretório raiz `backend`, um banco PostgreSQL na mesma região e health check em `/api/health`.
- **Frontend:** Static Site com diretório raiz `frontend/pomohistoria`. O build é `npm ci && npm run build`, a pasta publicada é `dist`, e é preciso um rewrite de `/*` para `/index.html`.

O passo a passo completo, as variáveis de produção, a tabela de rotas da API e as limitações conhecidas estão em [`backend/DEPLOY.md`](backend/DEPLOY.md).

## Fluxo de trabalho com Git

- **`dev`:** branch de desenvolvimento, para onde vão os commits do dia a dia.
- **`main`:** branch de produção. O Render publica automaticamente a cada atualização.

Para publicar, abra um Pull Request de `dev` para `main` no GitHub e faça o merge depois da revisão. As mensagens de commit seguem o padrão `feat:`, `fix:`, `refactor:` etc., em português.

## Documentação complementar

| Documento | Conteúdo |
|---|---|
| [`backend/README.md`](backend/README.md) | Rodar o backend, conta admin e testes |
| [`backend/DEPLOY.md`](backend/DEPLOY.md) | Deploy no Render, variáveis de produção, rotas da API e limitações |
| [`frontend/pomohistoria/README.md`](frontend/pomohistoria/README.md) | Integração com a API, páginas, variáveis do Vite e hospedagem |

## Autoria

Projeto desenvolvido na **FATEC**.

| Papel | Nome |
|---|---|
| Autor | Kauê de Oliveira Martins |
| Autor | Diego de Oliveira Ferreira |
| Orientadora | Juliana da Silva Linhares |
