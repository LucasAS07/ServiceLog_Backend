ALTER TABLE atendimento
    ADD COLUMN inicio_ts TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN fim_ts    TIMESTAMP WITHOUT TIME ZONE;


UPDATE atendimento
SET
    inicio_ts = dia + inicio,
    fim_ts = CASE
                 WHEN fim < inicio
                     THEN dia + fim + INTERVAL '1 day'
    ELSE dia + fim
END;

