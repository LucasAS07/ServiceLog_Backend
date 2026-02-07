create table roles(
                      id INTEGER not null primary key,
                      nome varchar(60) not null
);

INSERT INTO ROLES(ID,NOME) VALUES (1,'ADMIN');
INSERT INTO ROLES (ID, NOME) VALUES (2,'USER')