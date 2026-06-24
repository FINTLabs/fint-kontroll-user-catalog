update users
set status = 'DISABLED'
where status is null;

alter table users
    alter column status set not null;
