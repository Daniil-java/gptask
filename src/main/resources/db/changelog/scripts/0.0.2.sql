--liquibase formatted sql

--changeset DanielK:2

alter table tasks
alter column priority type text using priority::text;

alter table tasks
alter column status type text using status::text;