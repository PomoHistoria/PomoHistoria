package com.project.PomoHistoria;

import com.project.PomoHistoria.support.PostgresEmbutido;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Sobe a aplicação inteira contra um PostgreSQL real: valida migrações Flyway, mapeamento JPA e seed. */
@SpringBootTest
class PomoHistoriaApplicationTests extends PostgresEmbutido {

    @Test
    void contextLoads() {
    }
}
