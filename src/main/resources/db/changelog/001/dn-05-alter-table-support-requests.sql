-- Отмена миграции, которая добавляла last_message_id в support_requests

-- Шаг 1: Удаляем внешний ключ
ALTER TABLE support_requests
DROP CONSTRAINT uq_last_message_id;

-- Шаг 2: Удаляем ограничение уникальности
ALTER TABLE support_requests
DROP CONSTRAINT fk_last_message;

-- Шаг 3: Удаляем сам столбец
ALTER TABLE support_requests
DROP COLUMN last_message_id;