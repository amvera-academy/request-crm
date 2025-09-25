-- Промежуточная таблица для участников переписки
CREATE TABLE IF NOT EXISTS support_request_participants (
    support_request_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    PRIMARY KEY (support_request_id, participant_id),
    FOREIGN KEY (support_request_id) REFERENCES support_requests(id),
    FOREIGN KEY (participant_id) REFERENCES telegram_users(id)
);