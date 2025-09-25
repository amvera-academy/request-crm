-- Добавляем поле last_message_id в таблицу support_requests
ALTER TABLE support_requests
ADD COLUMN last_message_id BIGINT;

-- Добавляем внешний ключ для last_message_id
ALTER TABLE support_requests
ADD CONSTRAINT fk_last_message
FOREIGN KEY (last_message_id)
REFERENCES telegram_messages(id);

-- Делаем поле last_message_id уникальным, чтобы реализовать OneToOne
ALTER TABLE support_requests
ADD CONSTRAINT uq_last_message_id UNIQUE (last_message_id);