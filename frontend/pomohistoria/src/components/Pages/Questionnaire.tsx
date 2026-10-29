import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import logo from '../../assets/POMOHISTÓRIA.webp';
import type {
  Alternativa,
  EtapaProgresso,
  ProgressoQuestionario,
  Questao,
  ResultadoValidacao,
  RespostaQuestao,
} from '../../services/questionarioApi';
import {
  iniciarModulo,
  listarQuestoes,
  obterProgresso,
  validarEtapa,
} from '../../services/questionarioApi';
import { ApiError } from '../../services/http';
import { registrarModuloIniciado } from '../../hooks/useFundoDoModulo';
import { useSessao } from '../../hooks/useSessao';
import styles from '../Modal/Questionnaire.module.css';

function sessaoExpirou(error: unknown): boolean {
  return error instanceof ApiError && error.naoAutenticado;
}

/** Registra no servidor que o usuário abriu o módulo atual (troca o fundo da tela inicial). */
function marcarInicioDoModulo(usuarioId: string | undefined, progresso: ProgressoQuestionario) {
  const modulo = progresso.moduloAtual;
  if (modulo === null || !usuarioId) return;
  if (progresso.modulosIniciados.includes(modulo)) {
    registrarModuloIniciado(usuarioId, modulo);
    return;
  }
  iniciarModulo(modulo)
    .then(() => registrarModuloIniciado(usuarioId, modulo))
    .catch((error: unknown) => console.error('Não foi possível registrar o início do módulo.', error));
}

const alternativas: Alternativa[] = ['A', 'B', 'C', 'D', 'E'];
const modulos = [1, 2];
const partes = [1, 2, 3];

function obterTextoAlternativa(questao: Questao, alternativa: Alternativa): string {
  switch (alternativa) {
    case 'A': return questao.alternativaA;
    case 'B': return questao.alternativaB;
    case 'C': return questao.alternativaC;
    case 'D': return questao.alternativaD;
    case 'E': return questao.alternativaE;
  }
}

export default function Questionnaire() {
  const [progresso, setProgresso] = useState<ProgressoQuestionario | null>(null);
  const [questoes, setQuestoes] = useState<Questao[]>([]);
  const [respostas, setRespostas] = useState<Record<string, Alternativa>>({});
  const [carregando, setCarregando] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState('');
  const [precisaLogin, setPrecisaLogin] = useState(false);
  const [resultado, setResultado] = useState<ResultadoValidacao | null>(null);
  const usuarioId = useSessao().usuario?.id;

  useEffect(() => {
    let ativo = true;

    async function carregarQuestionario() {
      try {
        const dadosProgresso = await obterProgresso();
        if (!ativo) return;
        setProgresso(dadosProgresso);
        marcarInicioDoModulo(usuarioId, dadosProgresso);

        if (dadosProgresso.moduloAtual !== null && dadosProgresso.parteAtual !== null) {
          const dadosQuestoes = await listarQuestoes(
            dadosProgresso.moduloAtual,
            dadosProgresso.parteAtual
          );
          if (ativo) setQuestoes(dadosQuestoes);
        }
      } catch (error: unknown) {
        if (ativo) {
          setErro(error instanceof Error ? error.message : 'Não foi possível carregar o questionário.');
          setPrecisaLogin(sessaoExpirou(error));
        }
      } finally {
        if (ativo) setCarregando(false);
      }
    }

    void carregarQuestionario();
    return () => {
      ativo = false;
    };
  }, [usuarioId]);

  function selecionarResposta(id: string, alternativa: Alternativa) {
    setRespostas((atuais) => ({ ...atuais, [id]: alternativa }));
  }

  async function enviarRespostas() {
    if (
      progresso?.moduloAtual == null ||
      progresso.parteAtual == null ||
      questoes.length !== 4 ||
      Object.keys(respostas).length !== questoes.length
    ) {
      return;
    }

    const respostasEnviadas: RespostaQuestao[] = questoes.map((questao) => ({
      questaoId: questao.id,
      alternativa: respostas[questao.id],
    }));

    setEnviando(true);
    setErro('');
    setResultado(null);

    try {
      const validacao = await validarEtapa(
        progresso.moduloAtual,
        progresso.parteAtual,
        respostasEnviadas
      );
      setResultado(validacao);

      if (validacao.aprovado) {
        const progressoAtualizado = await obterProgresso();
        setProgresso(progressoAtualizado);
        // Se a aprovação liberou o módulo seguinte, ele está sendo aberto agora.
        marcarInicioDoModulo(usuarioId, progressoAtualizado);
        setRespostas({});
        if (progressoAtualizado.moduloAtual !== null && progressoAtualizado.parteAtual !== null) {
          setQuestoes(await listarQuestoes(
            progressoAtualizado.moduloAtual,
            progressoAtualizado.parteAtual
          ));
        } else {
          setQuestoes([]);
        }
      }
    } catch (error: unknown) {
      setErro(error instanceof Error ? error.message : 'Não foi possível validar suas respostas.');
      setPrecisaLogin(sessaoExpirou(error));
    } finally {
      setEnviando(false);
    }
  }

  const moduloAtual = progresso?.moduloAtual ?? 1;
  const parteAtual = progresso?.parteAtual ?? 1;
  const respondidas = Object.keys(respostas).length;

  return (
    <main className={styles.page}>
      <header className={styles.header}>
        <Link to="/" aria-label="Voltar ao início">
          <img className={styles.logo} src={logo} alt="PomoHistória" />
        </Link>
        <Link className={styles.backLink} to="/">Voltar ao início</Link>
      </header>

      <div className={styles.content}>
        <section className={styles.intro}>
          <p className={styles.eyebrow}>Jornada de aprendizagem</p>
          <h1>Questionário</h1>
          <p>
            Avance pelas partes do módulo. A próxima etapa será liberada após a
            validação do seu desempenho.
          </p>
        </section>

        {erro && (
          <div className={styles.errorMessage} role="alert">
            {erro}
            {precisaLogin && (
              <> <Link to="/login">Faça login</Link> para acessar seu progresso.</>
            )}
          </div>
        )}

        <section className={styles.moduleSection} aria-labelledby="modulos-title">
          <div className={styles.sectionHeading}>
            <div>
              <h2 id="modulos-title">Sua jornada</h2>
              <p>Conclua o primeiro módulo para liberar o próximo.</p>
            </div>
            <span className={styles.progressLabel}>
              {progresso?.modulosConcluidos ?? 0} de 2 módulos concluídos
            </span>
          </div>

          <div className={styles.moduleList}>
            {modulos.map((modulo) => {
              const etapasModulo = progresso?.etapas.filter((etapa) => etapa.modulo === modulo) ?? [];
              const liberado = etapasModulo.some((etapa) => etapa.liberada || etapa.concluida);
              const concluido = etapasModulo.length > 0 && etapasModulo.every((etapa) => etapa.concluida);

              return (
                <article
                  className={`${styles.moduleCard} ${liberado ? styles.moduleAvailable : styles.moduleLocked}`}
                  key={modulo}
                  aria-current={!progresso?.concluido && modulo === moduloAtual ? 'step' : undefined}
                >
                  <span className={styles.moduleNumber}>
                    {liberado ? String(modulo).padStart(2, '0') : '—'}
                  </span>
                  <span className={styles.moduleName}>Módulo {modulo}</span>
                  <span className={styles.moduleStatus}>
                    {concluido ? 'Concluído' : liberado ? 'Disponível' : 'Bloqueado'}
                  </span>
                </article>
              );
            })}
          </div>
        </section>

        <section className={styles.quizCard} aria-labelledby="part-title">
          {progresso?.concluido ? (
            <div className={styles.completionMessage} role="status">
              <p className={styles.eyebrow}>Jornada concluída</p>
              <h2 id="part-title">Parabéns!</h2>
              <p>Você concluiu todas as partes do questionário.</p>
            </div>
          ) : (
            <>
              <div className={styles.quizHeader}>
                <div>
                  <p className={styles.eyebrow}>Módulo {moduloAtual}</p>
                  <h2 id="part-title">Parte {parteAtual} <span>de 3</span></h2>
                </div>
                <span className={styles.questionCount}>4 questões</span>
              </div>

              <nav className={styles.partNavigation} aria-label={`Partes do módulo ${moduloAtual}`}>
                {partes.map((parte) => {
                  const etapa = progresso?.etapas.find(
                    (item: EtapaProgresso) => item.modulo === moduloAtual && item.parte === parte
                  );
                  const liberada = etapa?.liberada ?? (moduloAtual === 1 && parte === 1);

                  return (
                    <div
                      className={`${styles.part} ${parte === parteAtual ? styles.partCurrent : liberada ? '' : styles.partLocked}`}
                      key={parte}
                      aria-current={parte === parteAtual ? 'step' : undefined}
                      aria-label={`Parte ${parte}${liberada ? ', disponível' : ', bloqueada'}`}
                    >
                      <span>{liberada ? parte : '—'}</span>
                      <span>Parte {parte}</span>
                      {etapa?.concluida && <span>Concluída</span>}
                    </div>
                  );
                })}
              </nav>

              {carregando && (
                <p className={styles.message} role="status">Carregando progresso...</p>
              )}

              {!carregando && !erro && questoes.length === 0 && (
                <p className={styles.message}>Ainda não há questões cadastradas nesta parte.</p>
              )}

              {!carregando && questoes.length > 0 && (
                <div className={styles.questions}>
                  {questoes.map((questao, indice) => (
                    <fieldset className={styles.question} key={questao.id}>
                      <legend>
                        <span className={styles.questionNumber}>Questão {indice + 1}</span>
                        {questao.enunciado}
                      </legend>
                      <div className={styles.answers}>
                        {alternativas.map((alternativa) => {
                          const id = `questao-${questao.id}-${alternativa}`;
                          return (
                            <label
                              className={`${styles.answer} ${respostas[questao.id] === alternativa ? styles.answerSelected : ''}`}
                              htmlFor={id}
                              key={alternativa}
                            >
                              <input
                                id={id}
                                type="radio"
                                name={`questao-${questao.id}`}
                                value={alternativa}
                                checked={respostas[questao.id] === alternativa}
                                onChange={() => selecionarResposta(questao.id, alternativa)}
                              />
                              <span className={styles.answerLetter}>{alternativa}</span>
                              <span>{obterTextoAlternativa(questao, alternativa)}</span>
                            </label>
                          );
                        })}
                      </div>
                    </fieldset>
                  ))}
                </div>
              )}

              {resultado && (
                <p className={resultado.aprovado ? styles.successMessage : styles.message} role="status">
                  Você acertou {resultado.respostasCorretas} de {resultado.totalQuestoes} questões
                  ({resultado.percentual}%).{' '}
                  {resultado.aprovado
                    ? 'Etapa concluída; seu progresso foi atualizado.'
                    : `Você precisa de pelo menos ${resultado.percentualMinimo}% para avançar. Tente novamente.`}
                </p>
              )}

              <footer className={styles.quizFooter}>
                <p>{respondidas} de {questoes.length} respondidas</p>
                <button
                  className={styles.submitButton}
                  type="button"
                  disabled={enviando || carregando || questoes.length !== 4 || respondidas !== questoes.length}
                  onClick={() => void enviarRespostas()}
                >
                  {enviando ? 'Validando...' : 'Enviar respostas'}
                </button>
              </footer>
              <p className={styles.backendNote}>
                A aprovação exige no mínimo 70% de acertos. A correção e a liberação são validadas pelo servidor.
              </p>
            </>
          )}
        </section>
      </div>
    </main>
  );
}
