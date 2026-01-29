-- 1. Cria uma coluna nova como INTERVAL
ALTER TABLE atendimento
    ADD COLUMN tempo_total_interval INTERVAL;

-- 2. Converte os valores antigos TIME → INTERVAL
UPDATE atendimento
SET tempo_total_interval = tempo_total::INTERVAL;

-- 3. Remove a coluna antiga
ALTER TABLE atendimento
DROP COLUMN tempo_total;

-- 4. Renomeia para o nome original
ALTER TABLE atendimento
    RENAME COLUMN tempo_total_interval TO tempo_total;