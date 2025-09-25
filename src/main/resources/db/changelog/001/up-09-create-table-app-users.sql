CREATE TABLE if not exists app_users (
   id BIGSERIAL PRIMARY KEY,
   username VARCHAR(255) NOT NULL UNIQUE,
   password_hash VARCHAR(255) NOT NULL,
   is_account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
   is_account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
   is_credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
   is_enabled BOOLEAN NOT NULL DEFAULT TRUE,   role VARCHAR(50) NOT NULL
);