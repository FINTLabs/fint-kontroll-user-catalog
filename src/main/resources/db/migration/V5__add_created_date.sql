drop table if exists external_users;
alter table users ADD createddate timestamp;
alter table users ADD modifieddate timestamp;