--liquibase formatted sql

--changeset DanielK:4

alter table users add password text;

alter table users
    alter column telegram_id drop not null;

alter table users
    alter column chat_id drop not null;

alter table users
    alter column bot_state drop not null;



CREATE TABLE IF NOT EXISTS roles
(
    id serial not null primary key,
    status text
);

CREATE TABLE IF NOT EXISTS users_roles
(
    user_id int NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id  int NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

