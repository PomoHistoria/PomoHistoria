package com.project.PomoHistoria.questionario;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    private final QuestionarioRepository questionarioRepository;
    private final ObjectMapper objectMapper;

    public QuestionarioSeedLoader(QuestionarioRepository questionarioRepository, ObjectMapper objectMapper) {
        this.questionarioRepository = questionarioRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (questionarioRepository.count() > 0) {
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
                validarQuestaoSeed(caminho, moduloSeed.modulo(), parte.parte(), questaoSeed);

                Questionario questao = new Questionario();
                questao.setModulo(moduloSeed.modulo());
                questao.setParte(parte.parte());
                questao.setOrdem(questaoSeed.ordem());
                questao.setEnunciado(questaoSeed.enunciado());
                questao.setAlternativaA(questaoSeed.alternativas().get("A"));
                questao.setAlternativaB(questaoSeed.alternativas().get("B"));
                questao.setAlternativaC(questaoSeed.alternativas().get("C"));
                questao.setAlternativaD(questaoSeed.alternativas().get("D"));
                questao.setAlternativaE(questaoSeed.alternativas().get("E"));
                questao.setRespostaCorreta(questaoSeed.respostaCorreta().trim().toUpperCase(Locale.ROOT));
                questionarioRepository.save(questao);
            }
        }
    }

    private void validarQuestaoSeed(
            String caminho,
            int modulo,
            int parte,
            QuestaoSeed questao
    ) throws IOException {
        String identificacao = "Questão " + questao.ordem() + " do módulo " + modulo
                + ", parte " + parte + " em " + caminho;
        if (questao.enunciado() == null || questao.enunciado().isBlank()) {
            throw new IOException(identificacao + " precisa ter enunciado.");
        }
        if (questao.alternativas() == null || !List.of("A", "B", "C", "D", "E").stream()
                .allMatch(letra -> questao.alternativas().get(letra) != null
                        && !questao.alternativas().get(letra).isBlank())) {
            throw new IOException(identificacao + " precisa ter alternativas A, B, C, D e E.");
        }
        if (questao.respostaCorreta() == null
                || !List.of("A", "B", "C", "D", "E")
                        .contains(questao.respostaCorreta().trim().toUpperCase(Locale.ROOT))) {
            throw new IOException(identificacao + " precisa indicar uma resposta correta entre A e E.");
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
