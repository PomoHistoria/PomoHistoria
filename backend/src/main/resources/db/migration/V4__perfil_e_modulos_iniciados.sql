-- Dados extras do perfil. A foto é uma data URL (imagem já reduzida pelo navegador).
ALTER TABLE usuarios ADD COLUMN telefone VARCHAR(20);
ALTER TABLE usuarios ADD COLUMN foto VARCHAR(400000);

-- Registra a primeira vez que o usuário abriu cada módulo (usado para trocar o fundo da tela).
CREATE TABLE modulo_iniciado (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    modulo INTEGER NOT NULL CHECK (modulo BETWEEN 1 AND 2),
    iniciado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_modulo_iniciado_usuario_modulo UNIQUE (usuario_id, modulo)
);
