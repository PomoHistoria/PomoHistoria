# Deploy do backend (Render)

O backend é publicado com o [Dockerfile](./Dockerfile). No Render, crie um **Web Service** do tipo Docker com o diretório raiz `backend`, e um banco **PostgreSQL**.

## Variáveis de ambiente

O modelo completo, com comentários, está em [.env.example](./.env.example). O arquivo `.env` serve só para desenvolvimento local e não entra no git nem na imagem Docker.

### Obrigatórias

| Variável | Exemplo | Para quê |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host>:5432/<banco>` | Endereço do banco. O Render mostra a URL como `postgresql://usuario:senha@host:5432/banco`: use só `host:porta/banco`, com o prefixo `jdbc:postgresql://`. |
| `SPRING_DATASOURCE_USERNAME` | `pomohistoria` | Usuário do banco. |
| `SPRING_DATASOURCE_PASSWORD` | (segredo) | Senha do banco. |
| `CORS_ALLOWED_ORIGINS` | `https://pomohistoria.vercel.app` | URL(s) do frontend, separadas por vírgula e sem barra no final. `*` não é aceito. |

### Opcionais

| Variável | Padrão | Para quê |
|---|---|---|
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | vazio | Cria a conta ADMIN na primeira inicialização. Configure as duas juntas, com uma senha forte de até 72 bytes. |
| `SESSION_COOKIE_SAME_SITE` | `none` | Use `lax` se front e back estiverem no mesmo domínio (ex.: `app.site.com` e `api.site.com`). |
| `SESSION_COOKIE_SECURE` | `true` | Só deve ser `false` em desenvolvimento sem HTTPS. |
| `SESSION_TIMEOUT` | `30m` | Tempo de inatividade até a sessão expirar. |
| `LOGIN_MAX_FALHAS_POR_EMAIL` | `5` | Senhas erradas seguidas antes de bloquear aquele e-mail. |
| `LOGIN_MAX_FALHAS_POR_IP` | `50` | Senhas erradas de um mesmo IP antes de bloquear o IP. |
| `LOGIN_BLOQUEIO_MINUTOS` | `15` | Duração da janela de bloqueio. |
| `DB_POOL_SIZE` | `5` | Conexões simultâneas com o banco. |

O `PORT` é definido pelo próprio Render.

No frontend, configure `VITE_API_BASE_URL` com a URL pública do backend (ex.: `https://pomohistoria-api.onrender.com`).

## Health check

Configure o **Health Check Path** do Render como `/api/health`. Ele é público e não cria sessão.

## Banco de dados

As tabelas são criadas pelo Flyway na primeira inicialização (`db/migration`), e as questões são carregadas do seed. O Hibernate só valida o esquema e nunca o altera.

> Use um banco **vazio** no primeiro deploy. Se o banco já tiver tabelas criadas de outra forma (sem a tabela `flyway_schema_history`), o Flyway interrompe a inicialização para não sobrescrever nada.

## Como a autenticação funciona

- **Login:** cria uma sessão (cookie `JSESSIONID`, `HttpOnly`, `Secure` e `SameSite=None` por padrão) e troca o ID da sessão nesse momento.
- **Requisições:** a cada uma, o backend confere no banco se o usuário continua ativo e qual é o papel atual dele.
- **POST:** exige o header `X-XSRF-TOKEN` com o valor obtido em `GET /api/auth/csrf`. O frontend já faz isso.
- **Logout:** `POST /api/auth/logout` responde `204`.

## Rotas da API

Toda rota que não seja GET exige o header `X-XSRF-TOKEN`.

| Método | Rota | Acesso | Uso |
|---|---|---|---|
| GET | `/api/health` | público | Health check |
| GET | `/api/auth/csrf` | público | Token CSRF |
| POST | `/api/auth/register` | público | Cadastro |
| POST | `/api/auth/login` | público | Login |
| POST | `/api/auth/logout` | público | Logout (204) |
| GET | `/api/auth/me` | logado | Usuário atual (401 se não houver login) |
| PUT | `/api/usuarios/me` | logado | Salvar perfil; trocar email ou senha exige `senhaAtual` |
| PUT | `/api/usuarios/me/foto` | logado | Salvar ou remover (`null`) a foto |
| GET | `/api/questionario/progresso` | logado | Etapas, módulo atual e módulos já abertos |
| POST | `/api/questionario/modulos/{m}/iniciar` | logado | Marca que o módulo foi aberto (troca o fundo) |
| GET | `/api/questionario/modulos/{m}/partes/{p}` | logado | Questões da etapa, sem o gabarito |
| POST | `/api/questionario/modulos/{m}/partes/{p}/validar` | logado | Corrige as respostas |
| GET | `/api/admin/questoes` | ADMIN | Todas as questões, com o gabarito |
| PUT | `/api/admin/questoes/{id}` | ADMIN | Editar enunciado, alternativas e resposta correta |

### Limitações conhecidas

- **Bloqueio de login em memória:** o limite de tentativas vale para uma única instância e zera quando o servidor reinicia. Se escalar para mais de uma instância, troque por um armazenamento compartilhado (ex.: Redis).
- **Sessões em memória:** um restart ou um novo deploy desloga todos os usuários. Para manter as sessões, use Spring Session com JDBC ou Redis.
- **Cookies de terceiros:** com front e back em domínios diferentes, o cookie de sessão é um "cookie de terceiro". Safari e alguns navegadores com bloqueio estrito podem recusá-lo, e o login falha nesses casos. A solução definitiva é servir os dois no mesmo domínio: um domínio próprio com subdomínios, ou um proxy/rewrite de `/api` no host do frontend.
