package com.project.PomoHistoria.model;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuestionarioService {

    private final QuestionarioDAO questionarioDAO;

    public QuestionarioService(QuestionarioDAO questionarioDAO) {
        this.questionarioDAO = questionarioDAO;
    }

    @Transactional(readOnly = true)
    public List<Questionario> listarPorModuloEParte(int modulo, int parte) {
        validarModuloEParte(modulo, parte);
        return questionarioDAO.findAllByModuloAndParteOrderByOrdemAsc(modulo, parte);
    }

    public Questionario cadastrar(
            int modulo,
            int parte,
            int ordem,
            String enunciado,
            String alternativaA,
            String alternativaB,
            String alternativaC,
            String alternativaD,
            String respostaCorreta
    ) {
        validarModuloEParte(modulo, parte);
        if (ordem < 1 || ordem > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A ordem da questão deve estar entre 1 e 5.");
        }
        if (questionarioDAO.existsByModuloAndParteAndOrdem(modulo, parte, ordem)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma questão nessa posição.");
        }

        Questionario questao = new Questionario();
        questao.setModulo(modulo);
        questao.setParte(parte);
        questao.setOrdem(ordem);
        questao.setEnunciado(enunciado.trim());
        questao.setAlternativaA(alternativaA.trim());
        questao.setAlternativaB(alternativaB.trim());
        questao.setAlternativaC(alternativaC.trim());
        questao.setAlternativaD(alternativaD.trim());
        questao.setRespostaCorreta(respostaCorreta);

        return questionarioDAO.save(questao);
    }

    private void validarModuloEParte(int modulo, int parte) {
        if (modulo < 1 || modulo > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O módulo deve ser 1 ou 2.");
        }
        if (parte < 1 || parte > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A parte deve estar entre 1 e 3.");
        }
    }
}
