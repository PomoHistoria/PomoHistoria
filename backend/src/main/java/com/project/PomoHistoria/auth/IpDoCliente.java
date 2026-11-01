package com.project.PomoHistoria.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Descobre o IP de quem fez a requisição, usado no limite de tentativas de login.
 *
 * <p>No Render, toda requisição passa pela Cloudflare, e {@code getRemoteAddr()} devolve o IP da
 * Cloudflare, não o do usuário. Com {@code LOGIN_HEADER_IP=CF-Connecting-IP}, o IP vem desse header,
 * que a Cloudflare preenche e não deixa o cliente enviar (responde 403). Só configure um header que o
 * proxy da frente sobrescreva: se o cliente puder enviá-lo, ele troca de "IP" a cada tentativa.
 */
@Component
public class IpDoCliente {

    /** Só dígitos, letras hexadecimais, pontos e dois-pontos (IPv4 ou IPv6). */
    private static final Pattern IP = Pattern.compile("^[0-9A-Fa-f:.]{2,45}$");

    private final String header;

    public IpDoCliente(@Value("${app.security.login.header-ip:}") String header) {
        this.header = header == null || header.isBlank() ? null : header.trim();
    }

    public String de(HttpServletRequest request) {
        if (header != null) {
            String valor = request.getHeader(header);
            if (valor != null && IP.matcher(valor.trim()).matches()) {
                return valor.trim();
            }
        }
        return request.getRemoteAddr();
    }
}
