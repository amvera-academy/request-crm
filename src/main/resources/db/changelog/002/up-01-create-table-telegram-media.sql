-- Создает таблицу telegram_media для хранения метаданных файлов Telegram (скриншотов и т.д.)
CREATE TABLE if not exists telegram_media (
    id BIGSERIAL PRIMARY KEY,

-- fileId, который используется для вызова getFile. Может меняться со временем.
    telegram_file_id VARCHAR(255) NOT NULL,

-- file_unique_id. Постоянный идентификатор файла в Telegram для дедупликации.
    telegram_file_unique_id VARCHAR(255),

-- MIME-тип файла (например, image/jpeg). Длина 50 должна быть достаточной.
    mime_type VARCHAR(50),

-- Размер файла в байтах
    file_size INT,

-- Ширина изображения в пикселях
    width INT,

-- Высота изображения в пикселях
    height INT,

-- Флаг, указывающий, что файл был удален из хранилища Telegram (404/410)
    is_deleted_by_telegram BOOLEAN DEFAULT FALSE NOT NULL,

-- Ссылка на родительское сообщение
    message_id INT NOT NULL,

-- Тип использования медиа (PREVIEW, FULL_SIZE). Хранится как строка (VARCHAR) согласно JPA.
    usage_type VARCHAR(20) NOT NULL,


-- Ограничение внешнего ключа
    CONSTRAINT fk_telegram_media_message
        FOREIGN KEY (message_id)
            REFERENCES telegram_messages (telegram_message_id)
            ON DELETE CASCADE
);

-- Создание индексов для оптимизации запросов
-- Индекс по message_id для быстрого поиска медиафайлов по сообщению
CREATE INDEX if not exists idx_telegram_media_message_id ON telegram_media (message_id);

-- Индекс по telegram_file_unique_id для дедупликации и поиска
CREATE INDEX if not exists idx_telegram_media_file_unique_id ON telegram_media (telegram_file_unique_id);

-- Индекс по usage_type, часто будет использоваться для выборки PREVIEW или FULL_SIZE
CREATE INDEX if not exists idx_telegram_media_usage_type ON telegram_media (usage_type);