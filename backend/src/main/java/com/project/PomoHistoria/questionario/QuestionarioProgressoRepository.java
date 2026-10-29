package com.project.PomoHistoria.questionario;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionarioProgressoRepository extends JpaRepository<QuestionarioProgresso, UUID> {

    List<QuestionarioProgresso> findAllByUsuarioIdOrderByModuloAscParteAsc(UUID usuarioId);

    boolean existsByUsuarioIdAndModuloAndParte(UUID usuarioId, int modulo, int parte);
}
