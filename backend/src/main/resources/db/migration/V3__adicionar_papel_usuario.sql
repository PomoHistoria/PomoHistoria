ALTER TABLE usuarios
    ADD COLUMN papel VARCHAR(20) NOT NULL DEFAULT 'USER';

ALTER TABLE usuarios
    ADD CONSTRAINT ck_usuarios_papel CHECK (papel IN ('USER', 'ADMIN'));
