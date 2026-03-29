-- members 테이블
drop table if exists members cascade;
create table members
(
    id                 bigint primary key      not null auto_increment,
    username           varchar(50)             not null unique,
    password           varchar(255)            not null,
    name               varchar(50)             not null,
    role               varchar(20)             not null,
    created_date       timestamp default now() not null,
    last_modified_date timestamp
);

-- contents 테이블
drop table if exists contents cascade;
create table contents
(
    id                 bigint primary key not null auto_increment,
    title              varchar(100)       not null,
    description        text,
    view_count         bigint             not null default 0,
    deleted            boolean            not null default false,
    deleted_date       timestamp,
    created_date       timestamp          not null  default now(),
    created_by         varchar(50)        not null,
    last_modified_date timestamp,
    last_modified_by   varchar(50)
);
