package com.project.PomoHistoria.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminAccountInitializerTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Test
    void criaAdministradorSomenteQuandoConfigurado() throws Exception {
        when(usuarioRepository.findByEmail(eq("admin123@admin.com"))).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha-segura")).thenReturn("hash");
        var initializer = new AdminAccountInitializer(
                usuarioRepository,
                passwordEncoder,
                " Admin123@Admin.com ",
                "senha-segura"
        );

        initializer.run(new DefaultApplicationArguments(new String[0]));

        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario administrador = captor.getValue();
        assertEquals("admin123@admin.com", administrador.getEmail());
        assertEquals("hash", administrador.getSenha());
        assertEquals(Usuario.Papel.ADMIN, administrador.getPapel());
        assertTrue(administrador.isAtivo());
    }

    @Test
    void naoPromoveContaExistente() {
        Usuario usuario = new Usuario();
        usuario.setPapel(Usuario.Papel.USER);
        when(usuarioRepository.findByEmail("admin123@admin.com")).thenReturn(Optional.of(usuario));
        var initializer = new AdminAccountInitializer(
                usuarioRepository,
                passwordEncoder,
                "admin123@admin.com",
                "senha-segura"
        );

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> initializer.run(new DefaultApplicationArguments(new String[0]))
        );
        assertTrue(erro.getMessage().contains("não administrativa"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
