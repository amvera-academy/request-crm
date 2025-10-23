ALTER TABLE telegram_media
    DROP COLUMN retry_count,
    DROP COLUMN last_attempt_at;
    DROP COLUMN media_group_uuid;
