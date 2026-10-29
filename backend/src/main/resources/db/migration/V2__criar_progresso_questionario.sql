CREATE TABLE progresso_questionario (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    modulo INTEGER NOT NULL CHECK (modulo BETWEEN 1 AND 2),
    parte INTEGER NOT NULL CHECK (parte BETWEEN 1 AND 3),
    respostas_corretas INTEGER NOT NULL CHECK (respostas_corretas BETWEEN 0 AND 4),
    total_questoes INTEGER NOT NULL CHECK (total_questoes BETWEEN 1 AND 4),
    concluida_em TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_progresso_usuario_modulo_parte UNIQUE (usuario_id, modulo, parte)
);
