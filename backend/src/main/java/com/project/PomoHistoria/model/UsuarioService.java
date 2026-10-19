package com.project.PomoHistoria.model;

import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioDAO usuarioDAO, PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrar(String nome, String email, String senha) {
        String emailNormalizado = normalizarEmail(email);
        if (usuarioDAO.existsByEmail(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setAtivo(true);

        return usuarioDAO.save(usuario);
    }

    public Usuario autenticar(String email, String senha) {
        Usuario usuario = usuarioDAO.findByEmail(normalizarEmail(email))
                .orElseThrow(() -> credenciaisInvalidas());

        if (!usuario.isAtivo() || !passwordEncoder.matches(senha, usuario.getSenha())) {
            throw credenciaisInvalidas();
        }

        return usuario;
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException credenciaisInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos.");
    }
}
