-- Добавление столбцов 'retry_count' и 'last_attempt_at'
ALTER TABLE telegram_media
    ADD COLUMN if not exists retry_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN if not exists last_attempt_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN if not exists media_group_uuid VARCHAR(36);

