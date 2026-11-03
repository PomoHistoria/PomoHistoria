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
- `usuarioApi.ts`: perfil, foto, duração dos cronômetros e exclusão da conta;
- `questionarioApi.ts`: progresso, questões, início de módulo e envio das respostas;
- `adminApi.ts`: edição de questões.

A correção das respostas é feita no backend. Depois do envio, cada questão aparece marcada como certa ou errada. O gabarito nunca chega ao navegador do estudante. A lista completa de rotas está em [`backend/DEPLOY.md`](../../backend/DEPLOY.md#rotas-da-api).

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

Em produção, defina `VITE_API_BASE_URL` com a URL do **próprio frontend**, sem barra no final (ex.: `https://pomohistoria-1.onrender.com`), e gere um novo build. A variável pode ficar nas configurações da plataforma que faz o build ou em `.env.production`. As chamadas a `/api` são repassadas ao backend pela hospedagem (veja "Hospedagem" abaixo). Se a variável estiver vazia, o site mostra um aviso claro em vez de tentar acessar `localhost`.

Toda variável `VITE_*` vai para o JavaScript público do navegador. Não coloque senhas, tokens ou outras credenciais nela.

## Hospedagem

A hospedagem precisa de duas regras de rewrite, **nesta ordem**:

1. **`/api/*` → backend.** O site repassa as chamadas da API ao backend, e o navegador enxerga um site só. Sem essa regra, o cookie de sessão vira um "cookie de terceiro". O Safari e as abas anônimas bloqueiam esse tipo de cookie, e o login falha com "Sessão de segurança expirada".
2. **`/*` → `/index.html`.** O app usa `BrowserRouter`. Sem essa regra, recarregar a página em `/questionario` dá erro 404.

No **Render Static Site**, em Redirects/Rewrites, use Action **Rewrite** nas duas:

| Source | Destination |
|---|---|
| `/api/*` | `https://pomohistoria.onrender.com/api/*` |
| `/*` | `/index.html` |

Em Vercel ou Netlify, use as regras equivalentes.
