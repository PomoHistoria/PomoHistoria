package com.project.PomoHistoria.auth;

import com.project.PomoHistoria.common.exception.MuitasTentativasException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Limita tentativas de login com senha errada, por e-mail e por IP, numa janela de tempo.
 * Fica em memória: vale para uma única instância do backend e zera quando o servidor reinicia.
 */
@Service
public class LoginAttemptService {

    private static final int LIMPAR_ACIMA_DE = 10_000;

    private final Map<String, Janela> falhas = new ConcurrentHashMap<>();
    private final int maxFalhasPorEmail;
    private final int maxFalhasPorIp;
    private final Duration duracaoJanela;
    private final Clock clock;

    @Autowired
    public LoginAttemptService(
            @Value("${app.security.login.max-falhas-por-email:5}") int maxFalhasPorEmail,
            @Value("${app.security.login.max-falhas-por-ip:50}") int maxFalhasPorIp,
            @Value("${app.security.login.bloqueio-minutos:15}") int bloqueioMinutos
    ) {
        this(maxFalhasPorEmail, maxFalhasPorIp, Duration.ofMinutes(bloqueioMinutos), Clock.systemUTC());
    }

    LoginAttemptService(int maxFalhasPorEmail, int maxFalhasPorIp, Duration duracaoJanela, Clock clock) {
        this.maxFalhasPorEmail = maxFalhasPorEmail;
        this.maxFalhasPorIp = maxFalhasPorIp;
        this.duracaoJanela = duracaoJanela;
        this.clock = clock;
    }

    public void verificarBloqueio(String email, String ip) {
        if (atingiuLimite(chaveEmail(email), maxFalhasPorEmail) || atingiuLimite(chaveIp(ip), maxFalhasPorIp)) {
            throw new MuitasTentativasException("Muitas tentativas de login. Tente novamente em alguns minutos.");
        }
    }

    public void registrarFalha(String email, String ip) {
        incrementar(chaveEmail(email));
        incrementar(chaveIp(ip));
        if (falhas.size() > LIMPAR_ACIMA_DE) {
            Instant agora = clock.instant();
            falhas.entrySet().removeIf(entrada -> entrada.getValue().expirou(agora, duracaoJanela));
        }
    }

    /** O sucesso zera só o contador do e-mail; o do IP continua valendo. */
    public void registrarSucesso(String email) {
        falhas.remove(chaveEmail(email));
    }

    private boolean atingiuLimite(String chave, int limite) {
        Janela janela = falhas.get(chave);
        if (janela == null) {
            return false;
        }
        if (janela.expirou(clock.instant(), duracaoJanela)) {
            falhas.remove(chave, janela);
            return false;
        }
        return janela.quantidade() >= limite;
    }

    private void incrementar(String chave) {
        Instant agora = clock.instant();
        falhas.compute(chave, (k, janela) -> janela == null || janela.expirou(agora, duracaoJanela)
                ? new Janela(1, agora)
                : new Janela(janela.quantidade() + 1, janela.inicio()));
    }

    private static String chaveEmail(String email) {
        return "email:" + email;
    }

    private static String chaveIp(String ip) {
        return "ip:" + ip;
    }

    private record Janela(int quantidade, Instant inicio) {

        boolean expirou(Instant agora, Duration duracao) {
            return !agora.isBefore(inicio.plus(duracao));
        }
    }
}
