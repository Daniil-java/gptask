--liquibase formatted sql

--changeset DanielK:3

CREATE TABLE IF NOT EXISTS timers
(
    id                          serial not null primary key,
    user_id                     int not null references users(id),
    status                      text not null default 'PENDING',
    work_duration               int not null default 25,
    short_break_duration        int not null default 5,
    long_break_duration         int not null default 30,
    long_break_interval         int not null default 4,
    is_autostart_work           boolean not null default false,
    is_autostart_break          boolean not null default false,
    updated                     timestamp,
    created                     timestamp DEFAULT current_timestamp
);
