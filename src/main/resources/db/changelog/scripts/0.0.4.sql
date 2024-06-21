--liquibase formatted sql

--changeset DanielK:4

ALTER TABLE users
DROP COLUMN name,
    ADD telegram_id int NOT NULL unique,
    ADD chat_id int NOT NULL unique,
    ADD bot_state text NOT NULL,
    ADD username text,
    ADD firstname text,
    ADD lastname text,
    ADD language_code text,
    ADD last_updated_task_id int,
    ADD last_updated_task_message_id int,
    ADD last_updated_timer_message_id int,
    ADD last_updated_timer_settings_message_id int;

CREATE INDEX idx_telegram_id ON users (telegram_id);

CREATE INDEX idx_chat_id ON users (chat_id);

ALTER TABLE timers
    ADD interval int,
    ADD stop_time timestamp,
    ADD minute_to_stop int,
    ADD telegram_message_id int;