package com.project.PomoHistoria.usuario;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public AdminAccountInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String senha
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean emailConfigurado = !email.isBlank();
        boolean senhaConfigurada = !senha.isBlank();
        if (!emailConfigurado && !senhaConfigurada) {
            return;
        }
        if (!emailConfigurado || !senhaConfigurada) {
            throw new IllegalStateException("Configure ADMIN_EMAIL e ADMIN_PASSWORD juntos para criar o administrador.");
        }
        if (SenhaPolicy.excedeLimiteDeBytes(senha)) {
            throw new IllegalStateException("ADMIN_PASSWORD deve ter no máximo 72 bytes em UTF-8.");
        }

        String emailNormalizado = Usuario.normalizarEmail(email);
        var usuarioExistente = usuarioRepository.findByEmail(emailNormalizado);
        if (usuarioExistente.isPresent()) {
            if (usuarioExistente.get().getPapel() != Usuario.Papel.ADMIN) {
                throw new IllegalStateException(
                        "O e-mail configurado para ADMIN_EMAIL já pertence a uma conta não administrativa."
                );
            }
            return;
        }

        Usuario administrador = new Usuario();
        administrador.setNome("Administrador");
        administrador.setEmail(emailNormalizado);
        administrador.setSenha(passwordEncoder.encode(senha));
        administrador.setAtivo(true);
        administrador.setPapel(Usuario.Papel.ADMIN);
        usuarioRepository.save(administrador);
    }
}
