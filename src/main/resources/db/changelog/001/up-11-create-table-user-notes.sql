CREATE TABLE if not exists user_notes (
    id BIGSERIAL PRIMARY KEY,
    note_text TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    creator_id BIGINT,
    CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES telegram_users(id),
    CONSTRAINT fk_creator FOREIGN KEY (creator_id) REFERENCES app_users(id)
);