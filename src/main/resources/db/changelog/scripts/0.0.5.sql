--liquibase formatted sql

--changeset DanielK:5

alter table timer_tasks
drop constraint timer_tasks_pkey;

alter table timer_tasks
drop column id;

alter table timer_tasks
    add primary key (timer_id, task_id);