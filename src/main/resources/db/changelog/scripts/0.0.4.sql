--liquibase formatted sql

--changeset DanielK:4

CREATE TABLE IF NOT EXISTS timer_tasks
(
    id serial not null primary key,
    timer_id int NOT NULL REFERENCES timers(id) ON DELETE CASCADE,
    task_id  int NOT NULL REFERENCES tasks(id) ON DELETE CASCADE
);

alter table tasks
alter column priority set default 'WOULD';

alter table tasks
alter column status set default 'PLANNED';