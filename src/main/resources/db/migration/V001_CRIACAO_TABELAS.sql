
CREATE TABLE usuario (
    id      SERIAL PRIMARY KEY,
    nome    VARCHAR(255) NOT NULL,
    email   VARCHAR(100) NOT NULL UNIQUE,
    senha   VARCHAR(255) NOT NULL,
    status  BOOLEAN DEFAULT TRUE
);


CREATE TABLE atendimento (
    id              SERIAL PRIMARY KEY,
    colaborador     VARCHAR(255),
    dia             DATE NOT NULL,
    dia_da_semana   VARCHAR(100),
    inicio          TIME NOT NULL,
    fim             TIME NOT NULL,
    tempo_total     TIME,
    justificativa   TEXT,
    id_usuario      BIGINT NOT NULL,
    data_registro   TIMESTAMPTZ DEFAULT NOW(),

    CONSTRAINT fk_usuario_id_chamado
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);