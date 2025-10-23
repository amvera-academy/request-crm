ALTER TABLE app_users
    ADD COLUMN if not exists bot_token VARCHAR(255) NULL;

-- Дополнительно, если нужно добавить комментарий (поддерживается не всеми СУБД, например, PostgreSQL):
COMMENT ON COLUMN app_users.bot_token IS 'Токен для авторизации бота модератора.';