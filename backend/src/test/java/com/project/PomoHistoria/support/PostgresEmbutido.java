package com.project.PomoHistoria.support;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Base dos testes de integração: sobe um PostgreSQL embutido uma vez e aponta o datasource para ele. */
public abstract class PostgresEmbutido {

    private static final EmbeddedPostgres POSTGRES;

    static {
        try {
            POSTGRES = EmbeddedPostgres.builder().start();
        } catch (IOException exception) {
            throw new UncheckedIOException("Não foi possível iniciar o PostgreSQL embutido.", exception);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                POSTGRES.close();
            } catch (IOException ignorada) {
                // Encerramento do processo de teste; nada a fazer.
            }
        }));
    }

    @DynamicPropertySource
    static void configurarBanco(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "");
    }
}
