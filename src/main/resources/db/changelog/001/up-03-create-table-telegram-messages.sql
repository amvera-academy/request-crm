CREATE TABLE if not exists telegram_messages (
     telegram_message_id INT PRIMARY KEY,
     message_text TEXT,
     sender_id BIGINT NOT NULL,
     chat_id BIGINT NOT NULL,
     sent_at TIMESTAMP WITH TIME ZONE NOT NULL,
     is_edited BOOLEAN DEFAULT FALSE,
     is_media BOOLEAN DEFAULT FALSE,
     reply_to_message_id INT,
     FOREIGN KEY (sender_id) REFERENCES telegram_users(id),
     FOREIGN KEY (reply_to_message_id) REFERENCES telegram_messages(telegram_message_id)
);