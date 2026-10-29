# Backend do PomoHistória

API em Spring Boot 4 (Java 25) com PostgreSQL. As migrações do banco (Flyway) e as questões iniciais (seed) são aplicadas automaticamente quando a aplicação sobe.

Para publicar em produção, veja [DEPLOY.md](./DEPLOY.md).

## Rodar localmente

Pré-requisitos: Java 25 e PostgreSQL instalado e rodando.

1. Crie o banco (no `psql` ou no SQL Shell):

   ```sql
   CREATE DATABASE pomohistoria;
   ```

2. Revise o arquivo `backend/.env`. Ele fica fora do git; se não existir, copie de [.env.example](./.env.example). Nele, descomente e preencha `SPRING_DATASOURCE_PASSWORD` com a senha do seu PostgreSQL.

3. Inicie com o perfil `local`, que lê o `.env` e ajusta o cookie de sessão para `http://localhost`:

   ```powershell
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
   ```

A API fica em `http://localhost:8080`. O frontend (`npm run dev`) já aponta para esse endereço.

## Conta administradora

Preencha `ADMIN_EMAIL` e `ADMIN_PASSWORD` no `.env` (ou nas variáveis do Render) e reinicie o backend. A conta ADMIN é criada se o e-mail ainda não existir.

- **Contas existentes:** não são promovidas. Se o e-mail pertencer a uma conta comum, a inicialização é interrompida.
- **Senha:** trocar `ADMIN_PASSWORD` depois não altera a conta. Para trocar a senha, use a tela de perfil.
- **Depois de criar:** pode remover as duas variáveis.

Logado como admin, o menu **Perfil → Administração** abre a tela de edição das questões.

## Testes

```powershell
.\mvnw.cmd test
```

Os testes não precisam do PostgreSQL local nem do `.env`: eles sobem um PostgreSQL embutido e usam a configuração de produção.
