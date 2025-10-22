-- Добавление столбцов 'retry_count' и 'last_attempt_at'
ALTER TABLE telegram_media
    ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN last_attempt_at TIMESTAMP WITHOUT TIME ZONE;