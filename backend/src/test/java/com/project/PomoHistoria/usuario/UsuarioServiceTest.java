package com.project.PomoHistoria.usuario;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.PomoHistoria.common.exception.ConflitoException;
import com.project.PomoHistoria.common.exception.CredenciaisInvalidasException;
import com.project.PomoHistoria.common.exception.DadosInvalidosException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

class UsuarioServiceTest {

    private UsuarioRepository repository;
    private PasswordEncoder encoder;
    private UsuarioService service;

    @BeforeEach
    void configurar() {
        repository = mock(UsuarioRepository.class);
        encoder = mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).thenReturn("hash");
        service = new UsuarioService(repository, encoder);
    }

    @Test
    void emailInexistenteAindaRodaBcryptParaNaoRevelarContas() {
        when(repository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThrows(CredenciaisInvalidasException.class, () -> service.autenticar("X@x.com ", "qualquer-senha"));
        verify(encoder).matches(eq("qualquer-senha"), anyString());
    }

    @Test
    void contaInativaNaoEntra() {
        Usuario usuario = new Usuario();
        usuario.setSenha("hash");
        usuario.setAtivo(false);
        when(repository.findByEmail("x@x.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("senha-certa", "hash")).thenReturn(true);

        assertThrows(CredenciaisInvalidasException.class, () -> service.autenticar("x@x.com", "senha-certa"));
    }

    @Test
    void senhaAcimaDe72BytesNaoChegaAoBcrypt() {
        assertThrows(DadosInvalidosException.class, () -> service.cadastrar("Ana", "a@a.com", "ç".repeat(40)));
        assertThrows(CredenciaisInvalidasException.class, () -> service.autenticar("a@a.com", "ç".repeat(40)));
        verify(repository, never()).save(any());
    }

    @Test
    void cadastroSimultaneoComMesmoEmailViraConflito() {
        when(repository.save(any(Usuario.class))).thenThrow(new DataIntegrityViolationException("uk_usuarios_email"));

        assertThrows(ConflitoException.class, () -> service.cadastrar("Ana", "a@a.com", "senha-forte-1"));
    }
}
