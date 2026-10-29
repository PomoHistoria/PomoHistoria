package com.project.PomoHistoria.questionario;

import com.project.PomoHistoria.questionario.dto.QuestionarioDtos.QuestaoResponse;
import com.project.PomoHistoria.questionario.dto.QuestionarioDtos.ValidacaoRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Questionário do usuário logado. A edição de questões fica em QuestionarioAdminController. */
@RestController
@RequestMapping("/api/questionario")
public class QuestionarioController {

    private final QuestionarioService questionarioService;
    private final QuestionarioProgressoService progressoService;

    public QuestionarioController(
            QuestionarioService questionarioService,
            QuestionarioProgressoService progressoService
    ) {
        this.questionarioService = questionarioService;
        this.progressoService = progressoService;
    }

    @GetMapping("/modulos/{modulo}/partes/{parte}")
    public List<QuestaoResponse> listar(
            @PathVariable int modulo,
            @PathVariable int parte,
            @AuthenticationPrincipal UUID usuarioId
    ) {
        return questionarioService.listarPorModuloEParte(modulo, parte, usuarioId).stream()
                .map(QuestaoResponse::de)
                .toList();
    }

    @GetMapping("/progresso")
    public QuestionarioProgressoService.Progresso obterProgresso(@AuthenticationPrincipal UUID usuarioId) {
        return progressoService.obterProgresso(usuarioId);
    }

    /** Registra que o usuário abriu o módulo. Pode ser chamado várias vezes. */
    @PostMapping("/modulos/{modulo}/iniciar")
    public ResponseEntity<Void> iniciarModulo(
            @PathVariable int modulo,
            @AuthenticationPrincipal UUID usuarioId
    ) {
        progressoService.iniciarModulo(usuarioId, modulo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/modulos/{modulo}/partes/{parte}/validar")
    public QuestionarioService.Resultado validar(
            @PathVariable int modulo,
            @PathVariable int parte,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody ValidacaoRequest request
    ) {
        List<QuestionarioService.Resposta> respostas = request.respostas().stream()
                .map(resposta -> new QuestionarioService.Resposta(resposta.questaoId(), resposta.alternativa()))
                .toList();
        return questionarioService.validarRespostas(modulo, parte, usuarioId, respostas);
    }
}
