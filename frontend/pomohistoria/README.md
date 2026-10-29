# Frontend do PomoHistória

Aplicação React 19 + TypeScript, feita com Vite 8. A visão geral do projeto está no [README da raiz](../../README.md).

## Comandos

Requer Node.js 22.12 ou mais recente.

| Comando | O que faz |
|---|---|
| `npm install` | Instala as dependências |
| `npm run dev` | Sobe em `http://localhost:5173`, apontando para o backend local (`http://localhost:8080`) |
| `npm run build` | Checa os tipos e gera o site de produção em `dist/` |
| `npm run lint` | Roda o ESLint |
| `npm run preview` | Serve o `dist/` localmente, para conferir o build |

## Integração com a API

O frontend conversa com o backend Spring Boot por sessão (cookie). Todas as chamadas passam por `src/services/http.ts`. Ele busca o token CSRF em `GET /api/auth/csrf`, envia esse token no header `X-XSRF-TOKEN` em toda requisição que não seja GET e, quando o servidor responde 401, limpa a sessão e volta para o login.

Cada área tem seu serviço:
- `authApi.ts`: cadastro, login, logout e usuário atual;
- `usuarioApi.ts`: perfil, foto e exclusão da conta;
- `questionarioApi.ts`: progresso, questões, início de módulo e envio das respostas;
- `adminApi.ts`: edição de questões.

A correção das respostas é feita no backend; o gabarito nunca chega ao navegador do estudante. A lista completa de rotas está em [`backend/DEPLOY.md`](../../backend/DEPLOY.md#rotas-da-api).

## Páginas

| Rota | Acesso |
|---|---|
| `/login`, `/cadastro` | só visitantes (quem está logado vai para o início) |
| `/`, `/temporizador`, `/questionario`, `/profile` | logado |
| `/admin` | logado com papel ADMIN |

As regras ficam em `src/routes/RequireAuth.tsx`. O fundo do Início e do Temporizador muda conforme o módulo que o usuário já abriu (`src/hooks/useFundoDoModulo.ts`):
- nenhum módulo aberto: fundo inicial;
- Antiguidade Clássica: Grécia;
- Era Vargas: Era Vargas.

## Variáveis de ambiente

| Arquivo | Quando é usado | No git? |
|---|---|---|
| `.env.development` | `npm run dev` (aponta para `http://localhost:8080`) | sim |
| `.env.production` | `npm run build` | sim |
| `.env.local` | sobrescreve os de cima só na sua máquina | não |
| `.env.example` | modelo com explicações | sim |

Em produção, defina `VITE_API_BASE_URL` (URL do backend, sem barra no final) nas variáveis da plataforma que faz o build, ou preencha `.env.production`, e gere um novo build. Se a variável estiver vazia, o site mostra um aviso claro em vez de tentar acessar `localhost`.

Toda variável `VITE_*` vai para o JavaScript público do navegador. Não coloque senhas, tokens ou outras credenciais nela.

## Hospedagem

O app usa `BrowserRouter`. Configure na hospedagem uma regra de rewrite de `/*` para `/index.html`. Sem ela, recarregar a página em `/questionario` dá erro 404:

- **Render Static Site:** use Redirects/Rewrites com Source `/*`, Destination `/index.html` e Action **Rewrite**.
- **Vercel ou Netlify:** use a regra de rewrite equivalente.
