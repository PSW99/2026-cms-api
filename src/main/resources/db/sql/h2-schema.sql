-- TODO schema
-- example
create table members
(
    id                 bigint primary key      not null auto_increment,
    name               varchar(50)             not null,
    created_date       timestamp default now() not null,
    last_modified_date timestamp
);
