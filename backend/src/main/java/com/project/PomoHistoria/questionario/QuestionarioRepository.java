package com.project.PomoHistoria.questionario;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionarioRepository extends JpaRepository<Questionario, UUID> {

    List<Questionario> findAllByModuloAndParteOrderByOrdemAsc(int modulo, int parte);

    List<Questionario> findAllByOrderByModuloAscParteAscOrdemAsc();
}
