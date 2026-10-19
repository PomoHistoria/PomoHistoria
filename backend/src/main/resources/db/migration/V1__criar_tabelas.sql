CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha VARCHAR(60) NOT NULL,
    data_criacao TIMESTAMP WITH TIME ZONE NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);

CREATE TABLE questoes (
    id UUID PRIMARY KEY,
    modulo INTEGER NOT NULL CHECK (modulo BETWEEN 1 AND 2),
    parte INTEGER NOT NULL CHECK (parte BETWEEN 1 AND 3),
    ordem INTEGER NOT NULL CHECK (ordem BETWEEN 1 AND 5),
    enunciado VARCHAR(1000) NOT NULL,
    alternativa_a VARCHAR(500) NOT NULL,
    alternativa_b VARCHAR(500) NOT NULL,
    alternativa_c VARCHAR(500) NOT NULL,
    alternativa_d VARCHAR(500) NOT NULL,
    resposta_correta CHAR(1) NOT NULL CHECK (resposta_correta IN ('A', 'B', 'C', 'D')),
    CONSTRAINT uk_questoes_modulo_parte_ordem UNIQUE (modulo, parte, ordem)
);