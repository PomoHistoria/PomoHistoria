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
| `CORS_ALLOWED_ORIGINS` | `https://pomohistoria-1.onrender.com` | URL(s) do frontend, separadas por vírgula e sem barra no final. `*` não é aceito. |

### Opcionais

| Variável | Padrão | Para quê |
|---|---|---|
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | vazio | Cria a conta ADMIN na primeira inicialização. Configure as duas juntas, com uma senha forte de até 72 bytes. |
| `SESSION_COOKIE_SAME_SITE` | `none` | **Em produção, use `lax`**: o frontend repassa `/api` ao backend (veja abaixo), então o navegador vê um site só. `none` só serve se o frontend chamar o backend direto, de outro domínio. |
| `SESSION_COOKIE_SECURE` | `true` | Só deve ser `false` em desenvolvimento sem HTTPS. |
| `SESSION_TIMEOUT` | `30m` | Tempo de inatividade até a sessão expirar. |
| `LOGIN_MAX_FALHAS_POR_EMAIL` | `5` | Senhas erradas seguidas antes de bloquear aquele e-mail. |
| `LOGIN_MAX_FALHAS_POR_IP` | `50` | Senhas erradas de um mesmo IP antes de bloquear o IP. |
| `LOGIN_HEADER_IP` | vazio | **No Render, use `CF-Connecting-IP`.** É o header de onde sai o IP real do usuário (veja "IP do usuário" abaixo). Vazio usa o IP da conexão. |
| `LOGIN_BLOQUEIO_MINUTOS` | `15` | Duração da janela de bloqueio. |
| `DB_POOL_SIZE` | `5` | Conexões simultâneas com o banco. |

O `PORT` é definido pelo próprio Render.

## Frontend: proxy de `/api`

O navegador nunca chama o backend direto. O Static Site do frontend repassa `/api/*` ao backend, e assim frontend e API ficam no **mesmo site**.

Isso é necessário porque `onrender.com` é um sufixo público: `pomohistoria-1.onrender.com` e `pomohistoria.onrender.com` contam como sites diferentes. Nesse caso o cookie de sessão seria um "cookie de terceiro". O Safari (inclusive o Chrome no iPhone) e as abas anônimas recusam esse tipo de cookie, e o login falharia com "Sessão de segurança expirada".

No Static Site, em **Redirects/Rewrites**, nesta ordem:

| # | Source | Destination | Action |
|---|---|---|---|
| 1 | `/api/*` | `https://pomohistoria.onrender.com/api/*` | Rewrite |
| 2 | `/*` | `/index.html` | Rewrite |

A ordem importa: a regra `/*` captura tudo e precisa vir por último.

Nas variáveis do Static Site, `VITE_API_BASE_URL` é a URL do **próprio frontend** (`https://pomohistoria-1.onrender.com`), não a do backend. Depois de mudar, faça um novo deploy, porque a variável entra no build.

No backend, use `SESSION_COOKIE_SAME_SITE=lax`.

## IP do usuário

O limite de tentativas de login conta as falhas por IP. No Render, toda requisição passa pela Cloudflare. Por isso o IP da conexão (`getRemoteAddr()`) é sempre um IP da Cloudflare, e não o do usuário, tanto pelo proxy de `/api` quanto no acesso direto ao backend.

Com `LOGIN_HEADER_IP=CF-Connecting-IP`, o IP vem desse header. Ele é confiável porque a Cloudflare o preenche com o IP real e recusa (erro 1000, HTTP 403) qualquer requisição em que o cliente tente enviá-lo. Isso foi conferido no proxy e no acesso direto.

Não use o `X-Forwarded-For`: o primeiro item dele é o que o cliente mandar.

## Health check

Configure o **Health Check Path** do Render como `/api/health`. Ele é público e não cria sessão.

## Banco de dados

As tabelas são criadas pelo Flyway na primeira inicialização (`db/migration`), e as questões são carregadas do seed. O Hibernate só valida o esquema e nunca o altera.

> Use um banco **vazio** no primeiro deploy. Se o banco já tiver tabelas criadas de outra forma (sem a tabela `flyway_schema_history`), o Flyway interrompe a inicialização para não sobrescrever nada.

## Como a autenticação funciona

- **Login:** cria uma sessão (cookie `JSESSIONID`, `HttpOnly`, `Secure` e `SameSite=Lax` em produção) e troca o ID da sessão nesse momento.
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
| DELETE | `/api/usuarios/me` | logado (só USER) | Excluir a própria conta, com `senhaAtual`; apaga o progresso junto e encerra a sessão (204). ADMIN recebe 403 |
| GET | `/api/questionario/progresso` | logado | Etapas, módulo atual e módulos já abertos |
| POST | `/api/questionario/modulos/{m}/iniciar` | logado | Marca que o módulo foi aberto (troca o fundo) |
| GET | `/api/questionario/modulos/{m}/partes/{p}` | logado | Questões da etapa, sem o gabarito |
| POST | `/api/questionario/modulos/{m}/partes/{p}/validar` | logado | Corrige as respostas |
| GET | `/api/admin/questoes` | ADMIN | Todas as questões, com o gabarito |
| PUT | `/api/admin/questoes/{id}` | ADMIN | Editar enunciado, alternativas e resposta correta |

### Limitações conhecidas

- **Bloqueio de login em memória:** o limite de tentativas vale para uma única instância e zera quando o servidor reinicia. Se escalar para mais de uma instância, troque por um armazenamento compartilhado (ex.: Redis).
- **Sessões em memória:** um restart ou um novo deploy desloga todos os usuários. Para manter as sessões, use Spring Session com JDBC ou Redis.
- **Alternativa ao proxy:** um domínio próprio com subdomínios (ex.: `app.dominio.com.br` e `api.dominio.com.br`) também deixa os dois no mesmo site, sem proxy no meio.
