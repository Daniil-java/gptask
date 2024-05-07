--liquibase formatted sql

--changeset DanielK:1
CREATE TABLE IF NOT EXISTS users
(
    id              serial primary key,
    name            text,
    created         timestamp DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS tasks
(
    id              serial not null primary key,
    user_id         int not null references users(id),
    parent_id       int references tasks(id),
    name            text,
    priority        smallint not null DEFAULT 0,
    status          smallint not null DEFAULT 0,
    comment         text,
    updated         timestamp,
    created         timestamp DEFAULT current_timestamp
    );

CREATE INDEX parent_id ON tasks(parent_id);

