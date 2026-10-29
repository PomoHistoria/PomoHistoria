import { useEffect, useMemo, useState } from 'react';
import type { SubmitEvent } from 'react';
import { Link } from 'react-router-dom';
import logo from '../../assets/POMOHISTÓRIA.webp';
import { adminApi } from '../../services/adminApi';
import type { QuestaoAdmin, QuestaoEdicao } from '../../services/adminApi';
import type { Alternativa } from '../../services/questionarioApi';
import pageStyles from '../Modal/Questionnaire.module.css';
import styles from '../Modal/Admin.module.css';

const ALTERNATIVAS: Alternativa[] = ['A', 'B', 'C', 'D', 'E'];
const NOME_DO_MODULO: Record<number, string> = {
  1: 'Antiguidade Clássica',
  2: 'Era Vargas',
};

type CampoAlternativa = `alternativa${Alternativa}`;

function paraEdicao(questao: QuestaoAdmin): QuestaoEdicao {
  return {
    enunciado: questao.enunciado,
    alternativaA: questao.alternativaA,
    alternativaB: questao.alternativaB,
    alternativaC: questao.alternativaC,
    alternativaD: questao.alternativaD,
    alternativaE: questao.alternativaE,
    respostaCorreta: questao.respostaCorreta,
  };
}

function iguais(a: QuestaoEdicao, b: QuestaoEdicao): boolean {
  return (Object.keys(a) as (keyof QuestaoEdicao)[]).every((campo) => a[campo] === b[campo]);
}

function validar(dados: QuestaoEdicao): string | null {
  if (!dados.enunciado.trim()) return 'O enunciado é obrigatório.';
  if (dados.enunciado.length > 1000) return 'O enunciado deve ter no máximo 1000 caracteres.';
  for (const letra of ALTERNATIVAS) {
    const texto = dados[`alternativa${letra}`];
    if (!texto.trim()) return `A alternativa ${letra} é obrigatória.`;
    if (texto.length > 500) return 'Cada alternativa deve ter no máximo 500 caracteres.';
  }
  return null;
}

export default function Admin() {
  const [questoes, setQuestoes] = useState<QuestaoAdmin[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erroLista, setErroLista] = useState('');
  const [selecionadaId, setSelecionadaId] = useState<string | null>(null);
  const [rascunho, setRascunho] = useState<QuestaoEdicao | null>(null);
  const [salvando, setSalvando] = useState(false);
  const [feedback, setFeedback] = useState<{ tipo: 'sucesso' | 'erro'; texto: string } | null>(null);

  useEffect(() => {
    let ativo = true;
    adminApi
      .listarQuestoes()
      .then((lista) => {
        if (!ativo) return;
        setQuestoes(lista);
        if (lista.length > 0) {
          setSelecionadaId(lista[0].id);
          setRascunho(paraEdicao(lista[0]));
        }
      })
      .catch((error: unknown) => {
        if (ativo) setErroLista(error instanceof Error ? error.message : 'Não foi possível carregar as questões.');
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });
    return () => {
      ativo = false;
    };
  }, []);

  const selecionada = questoes.find((questao) => questao.id === selecionadaId) ?? null;
  const alterada = selecionada !== null && rascunho !== null && !iguais(paraEdicao(selecionada), rascunho);

  // Agrupa por módulo e parte para a lista lateral.
  const grupos = useMemo(() => {
    const mapa = new Map<string, { modulo: number; parte: number; questoes: QuestaoAdmin[] }>();
    for (const questao of questoes) {
      const chave = `${questao.modulo}-${questao.parte}`;
      if (!mapa.has(chave)) mapa.set(chave, { modulo: questao.modulo, parte: questao.parte, questoes: [] });
      mapa.get(chave)!.questoes.push(questao);
    }
    return [...mapa.values()];
  }, [questoes]);

  function selecionar(questao: QuestaoAdmin) {
    if (questao.id === selecionadaId) return;
    if (alterada && !window.confirm('Há alterações não salvas nesta questão. Descartar?')) return;
    setSelecionadaId(questao.id);
    setRascunho(paraEdicao(questao));
    setFeedback(null);
  }

  function alterarCampo<K extends keyof QuestaoEdicao>(campo: K, valor: QuestaoEdicao[K]) {
    setRascunho((atual) => (atual ? { ...atual, [campo]: valor } : atual));
    setFeedback(null);
  }

  async function salvar(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selecionada || !rascunho) return;
    const erro = validar(rascunho);
    if (erro) {
      setFeedback({ tipo: 'erro', texto: erro });
      return;
    }

    setSalvando(true);
    setFeedback(null);
    try {
      const atualizada = await adminApi.editarQuestao(selecionada.id, rascunho);
      setQuestoes((lista) => lista.map((questao) => (questao.id === atualizada.id ? atualizada : questao)));
      setRascunho(paraEdicao(atualizada));
      setFeedback({ tipo: 'sucesso', texto: 'Questão salva.' });
    } catch (error) {
      setFeedback({ tipo: 'erro', texto: error instanceof Error ? error.message : 'Não foi possível salvar.' });
    } finally {
      setSalvando(false);
    }
  }

  return (
    <main className={pageStyles.page}>
      <header className={pageStyles.header}>
        <Link className={pageStyles.logoLink} to="/" aria-label="Voltar ao início">
          <img className={pageStyles.logo} src={logo} alt="PomoHistória" />
        </Link>
        <Link className={pageStyles.backLink} to="/">Voltar ao início</Link>
      </header>

      <div className={styles.content}>
        <section className={pageStyles.intro}>
          <p className={pageStyles.eyebrow}>Administração</p>
          <h1>Editar questões</h1>
          <p>Escolha uma questão para alterar o enunciado, as alternativas ou a resposta correta.</p>
        </section>

        {carregando && <p className={pageStyles.message} role="status">Carregando questões...</p>}
        {erroLista && <div className={pageStyles.errorMessage} role="alert">{erroLista}</div>}

        {!carregando && !erroLista && (
          <div className={styles.layout}>
            <nav className={styles.lista} aria-label="Questões">
              {grupos.map((grupo) => (
                <div key={`${grupo.modulo}-${grupo.parte}`} className={styles.grupo}>
                  <p className={styles.grupoTitulo}>
                    Módulo {grupo.modulo} · {NOME_DO_MODULO[grupo.modulo] ?? ''}
                    <span>Parte {grupo.parte}</span>
                  </p>
                  {grupo.questoes.map((questao) => (
                    <button
                      key={questao.id}
                      type="button"
                      className={`${styles.itemQuestao} ${questao.id === selecionadaId ? styles.itemSelecionado : ''}`}
                      aria-current={questao.id === selecionadaId ? 'true' : undefined}
                      onClick={() => selecionar(questao)}
                    >
                      <strong>Questão {questao.ordem}</strong>
                      <span>{questao.enunciado}</span>
                    </button>
                  ))}
                </div>
              ))}
            </nav>

            {selecionada && rascunho && (
              <form className={styles.editor} onSubmit={salvar} noValidate>
                <p className={pageStyles.eyebrow}>
                  Módulo {selecionada.modulo} · Parte {selecionada.parte} · Questão {selecionada.ordem}
                </p>

                <label className={styles.campo}>
                  <span>Enunciado</span>
                  <textarea
                    value={rascunho.enunciado}
                    onChange={(event) => alterarCampo('enunciado', event.target.value)}
                    maxLength={1000}
                    rows={6}
                  />
                  <small>{rascunho.enunciado.length}/1000</small>
                </label>

                <fieldset className={styles.alternativas}>
                  <legend>Alternativas (marque a correta)</legend>
                  {ALTERNATIVAS.map((letra) => {
                    const campo: CampoAlternativa = `alternativa${letra}`;
                    return (
                      <div key={letra} className={styles.alternativa}>
                        <label className={styles.correta} title={`Marcar ${letra} como correta`}>
                          <input
                            type="radio"
                            name="respostaCorreta"
                            value={letra}
                            checked={rascunho.respostaCorreta === letra}
                            onChange={() => alterarCampo('respostaCorreta', letra)}
                          />
                          <span>{letra}</span>
                        </label>
                        <textarea
                          aria-label={`Texto da alternativa ${letra}`}
                          value={rascunho[campo]}
                          onChange={(event) => alterarCampo(campo, event.target.value)}
                          maxLength={500}
                          rows={2}
                        />
                      </div>
                    );
                  })}
                </fieldset>

                {feedback && (
                  <p
                    className={feedback.tipo === 'erro' ? pageStyles.errorMessage : pageStyles.successMessage}
                    role={feedback.tipo === 'erro' ? 'alert' : 'status'}
                  >
                    {feedback.texto}
                  </p>
                )}

                <div className={styles.acoes}>
                  <button
                    type="button"
                    className={styles.descartar}
                    disabled={!alterada || salvando}
                    onClick={() => {
                      setRascunho(paraEdicao(selecionada));
                      setFeedback(null);
                    }}
                  >
                    Descartar alterações
                  </button>
                  <button type="submit" className={pageStyles.submitButton} disabled={!alterada || salvando}>
                    {salvando ? 'Salvando...' : 'Salvar questão'}
                  </button>
                </div>
              </form>
            )}
          </div>
        )}
      </div>
    </main>
  );
}
