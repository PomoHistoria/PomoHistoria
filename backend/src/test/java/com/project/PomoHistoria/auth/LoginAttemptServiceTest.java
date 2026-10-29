package com.project.PomoHistoria.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.project.PomoHistoria.common.exception.MuitasTentativasException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    private final RelogioAjustavel relogio = new RelogioAjustavel();
    private final LoginAttemptService service = new LoginAttemptService(3, 5, Duration.ofMinutes(15), relogio);

    @Test
    void bloqueiaEmailAposLimiteELiberaDepoisDaJanela() {
        for (int i = 0; i < 3; i++) {
            service.registrarFalha("a@a.com", "1.1.1.1");
        }
        assertThrows(MuitasTentativasException.class, () -> service.verificarBloqueio("a@a.com", "2.2.2.2"));

        relogio.avancar(Duration.ofMinutes(15));
        assertDoesNotThrow(() -> service.verificarBloqueio("a@a.com", "2.2.2.2"));
    }

    @Test
    void bloqueiaIpQueTentaVariosEmails() {
        for (int i = 0; i < 5; i++) {
            service.registrarFalha("conta" + i + "@a.com", "9.9.9.9");
        }
        assertThrows(MuitasTentativasException.class, () -> service.verificarBloqueio("outra@a.com", "9.9.9.9"));
        assertDoesNotThrow(() -> service.verificarBloqueio("outra@a.com", "8.8.8.8"));
    }

    @Test
    void sucessoZeraContadorDoEmail() {
        service.registrarFalha("b@b.com", "1.1.1.1");
        service.registrarFalha("b@b.com", "1.1.1.1");
        service.registrarSucesso("b@b.com");
        service.registrarFalha("b@b.com", "1.1.1.1");
        assertDoesNotThrow(() -> service.verificarBloqueio("b@b.com", "1.1.1.1"));
    }

    private static final class RelogioAjustavel extends Clock {

        private Instant agora = Instant.parse("2026-01-01T00:00:00Z");

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }
}
