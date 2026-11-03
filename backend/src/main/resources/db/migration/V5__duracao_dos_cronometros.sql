-- Última duração usada em cada cronômetro Pomodoro, para valer em qualquer aparelho.
ALTER TABLE usuarios ADD COLUMN duracao_foco_minutos INTEGER NOT NULL DEFAULT 25
    CONSTRAINT ck_usuarios_duracao_foco CHECK (duracao_foco_minutos BETWEEN 1 AND 99);
ALTER TABLE usuarios ADD COLUMN duracao_descanso_minutos INTEGER NOT NULL DEFAULT 5
    CONSTRAINT ck_usuarios_duracao_descanso CHECK (duracao_descanso_minutos BETWEEN 1 AND 99);
