ALTER TABLE usuario
    ADD COLUMN role_id BIGINT;

ALTER TABLE usuario
    ADD CONSTRAINT fk_usuario_role
        FOREIGN KEY (role_id)
            REFERENCES roles(id);


UPDATE usuario
SET role_id = 2
WHERE role_id IS NULL;