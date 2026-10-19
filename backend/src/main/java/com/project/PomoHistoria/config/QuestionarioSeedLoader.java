package com.project.PomoHistoria.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.PomoHistoria.model.Questionario;
import com.project.PomoHistoria.model.QuestionarioDAO;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class QuestionarioSeedLoader implements ApplicationRunner {

    private final QuestionarioDAO questionarioDAO;
    private final ObjectMapper objectMapper;

    public QuestionarioSeedLoader(QuestionarioDAO questionarioDAO, ObjectMapper objectMapper) {
        this.questionarioDAO = questionarioDAO;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (questionarioDAO.count() > 0) {
            return;
        }

        for (String arquivo : List.of(
                "db/seed/questionario/modulo-1.json",
                "db/seed/questionario/modulo-2.json"
        )) {
            carregarArquivo(arquivo);
        }
    }

    private void carregarArquivo(String caminho) throws IOException {
        ClassPathResource resource = new ClassPathResource(caminho);
        if (!resource.exists()) {
            return;
        }

        ModuloSeed moduloSeed = objectMapper.readValue(resource.getInputStream(), ModuloSeed.class);
        if (moduloSeed == null || moduloSeed.partes() == null) {
            return;
        }

        for (ParteSeed parte : moduloSeed.partes()) {
            if (parte == null || parte.questoes() == null) {
                continue;
            }

            for (QuestaoSeed questaoSeed : parte.questoes()) {
                if (questaoSeed == null) {
                    continue;
                }

                Questionario questao = new Questionario();
                questao.setModulo(moduloSeed.modulo());
                questao.setParte(parte.parte());
                questao.setOrdem(questaoSeed.ordem());
                questao.setEnunciado(questaoSeed.enunciado());
                questao.setAlternativaA(questaoSeed.alternativas().get("A"));
                questao.setAlternativaB(questaoSeed.alternativas().get("B"));
                questao.setAlternativaC(questaoSeed.alternativas().get("C"));
                questao.setAlternativaD(questaoSeed.alternativas().get("D"));
                questao.setRespostaCorreta(questaoSeed.respostaCorreta().trim().toUpperCase(Locale.ROOT));
                questionarioDAO.save(questao);
            }
        }
    }

    private record ModuloSeed(
            int modulo,
            List<ParteSeed> partes
    ) {
    }

    private record ParteSeed(
            int parte,
            List<QuestaoSeed> questoes
    ) {
    }

    private record QuestaoSeed(
            int ordem,
            String enunciado,
            @JsonProperty("alternativas") Map<String, String> alternativas,
            @JsonProperty("respostaCorreta") String respostaCorreta
    ) {
    }
}
