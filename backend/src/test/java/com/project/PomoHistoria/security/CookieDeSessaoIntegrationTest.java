package com.project.PomoHistoria.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.project.PomoHistoria.support.PostgresEmbutido;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Sobe o Tomcat real com a configuração de produção e confere os atributos do cookie de sessão. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CookieDeSessaoIntegrationTest extends PostgresEmbutido {

    @LocalServerPort
    private int porta;

    @Test
    void cookieDeSessaoSaiSeguroParaUsoEntreDominios() throws Exception {
        HttpResponse<String> resposta = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + porta + "/api/auth/csrf")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, resposta.statusCode());
        String cookie = resposta.headers().allValues("Set-Cookie").stream()
                .filter(valor -> valor.startsWith("JSESSIONID="))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Cookie de sessão não enviado"));
        assertTrue(cookie.contains("HttpOnly"), cookie);
        assertTrue(cookie.contains("Secure"), cookie);
        assertTrue(cookie.contains("SameSite=None"), cookie);
    }

    @Test
    void headersDeSegurancaPadraoEstaoPresentes() throws Exception {
        HttpResponse<String> resposta = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + porta + "/api/health")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, resposta.statusCode());
        assertEquals("nosniff", resposta.headers().firstValue("X-Content-Type-Options").orElse(null));
        assertEquals("DENY", resposta.headers().firstValue("X-Frame-Options").orElse(null));
        assertTrue(resposta.headers().firstValue("Set-Cookie").isEmpty(), "O health check não deve criar sessão");
    }
}
