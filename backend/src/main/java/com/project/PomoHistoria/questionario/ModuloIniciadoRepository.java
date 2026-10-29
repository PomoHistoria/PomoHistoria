package com.project.PomoHistoria.questionario;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuloIniciadoRepository extends JpaRepository<ModuloIniciado, UUID> {

    List<ModuloIniciado> findAllByUsuarioIdOrderByModuloAsc(UUID usuarioId);

    boolean existsByUsuarioIdAndModulo(UUID usuarioId, int modulo);
}
