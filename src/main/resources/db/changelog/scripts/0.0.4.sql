--liquibase formatted sql

--changeset DanielK:4

ALTER TABLE users
DROP COLUMN name,
    ADD telegram_id int NOT NULL,
    ADD chat_id int NOT NULL,
    ADD bot_state text NOT NULL,
    ADD username text,
    ADD firstname text,
    ADD lastname text,
    ADD language_code text;

