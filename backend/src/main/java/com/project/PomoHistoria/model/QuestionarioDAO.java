package com.project.PomoHistoria.model;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionarioDAO extends JpaRepository<Questionario, UUID> {

    List<Questionario> findAllByModuloAndParteOrderByOrdemAsc(int modulo, int parte);

    boolean existsByModuloAndParteAndOrdem(int modulo, int parte, int ordem);
}
