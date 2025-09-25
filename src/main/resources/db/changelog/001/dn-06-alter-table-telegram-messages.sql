-- Отмена миграции, которая добавляла support_request_id в telegram_messages

-- Шаг 1: Удаляем внешний ключ
ALTER TABLE telegram_messages
    DROP CONSTRAINT fk_support_request;

-- Шаг 2: Удаляем сам столбец
ALTER TABLE telegram_messages
    DROP COLUMN support_request_id;