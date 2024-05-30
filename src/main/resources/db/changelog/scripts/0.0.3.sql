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

CREATE TABLE IF NOT EXISTS timer_tasks
(
    timer_id int NOT NULL REFERENCES timers(id) ON DELETE CASCADE,
    task_id  int NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    PRIMARY KEY (timer_id, task_id)
);

alter table tasks
    alter column priority set default 'WOULD';

alter table tasks
    alter column status set default 'PLANNED';

