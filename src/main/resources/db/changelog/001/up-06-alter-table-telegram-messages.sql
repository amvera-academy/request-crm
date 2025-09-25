-- Добавляем поле support_request_id в таблицу telegram_messages
ALTER TABLE telegram_messages
ADD COLUMN support_request_id BIGINT;

-- Добавляем внешний ключ для support_request_id
ALTER TABLE telegram_messages
ADD CONSTRAINT fk_support_request
FOREIGN KEY (support_request_id)
REFERENCES support_requests(id);