create database if not exists climate;
use climate;

create table province_region_code
(
    id          bigint primary key auto_increment,
    region_code varchar(20) not null unique
);

create table city_region_code
(
    id                      bigint primary key auto_increment,
    region_code             varchar(20) not null unique,
    x                       int         not null,
    y                       int         not null,
    province_region_code_id bigint,
    foreign key (province_region_code_id) references province_region_code (id) on delete set null
);

create table short_grid
(
    id             bigint primary key auto_increment,
    announce_time  datetime not null,
    effective_time datetime not null,
    x              int      not null,
    y              int      not null,
    pop            int,
    temp           int,
    unique key unique_set (announce_time, effective_time, x, y)
);

create table short_land
(
    id                  bigint primary key auto_increment,
    announce_time       datetime not null,
    effective_time      datetime not null,
    city_region_code_id bigint   not null,
    pop                 int,
    temp                int,
    rain_type           int,
    foreign key (city_region_code_id) references city_region_code (id) on delete cascade
);

create table mid_pop
(
    id                      bigint primary key auto_increment,
    announce_time           datetime not null,
    effective_time          datetime not null,
    province_region_code_id bigint   not null,
    pop                     int      not null,
    foreign key (province_region_code_id) references province_region_code (id) on delete cascade
);

create table mid_temperature
(
    id                  bigint primary key auto_increment,
    announce_time       datetime not null,
    effective_time      datetime not null,
    city_region_code_id bigint   not null,
    max_temp            int,
    min_temp            int,
    foreign key (city_region_code_id) references city_region_code (id) on delete cascade
);

create table warning_region_mapping
(
    region_code         varchar(16) not null,
    warning_region_code varchar(16) not null,
    primary key (region_code, warning_region_code)
);

create table warning_current
(
    warning_region_code varchar(16) not null,
    kind                varchar(20) not null,
    level               varchar(12) not null,
    announce_time       datetime    not null,
    effective_time      datetime    not null,
    primary key (warning_region_code, kind)
);

create table warning_event
(
    id                  bigint auto_increment primary key,
    warning_region_code varchar(16) not null,
    kind                varchar(20) not null,
    level               varchar(12) not null,
    prev_level          varchar(12),
    event_type          varchar(12) not null,
    announce_time       datetime    not null,
    effective_time      datetime    not null,
    index idx_wrn_reg_kind_id (warning_region_code, kind, id)
);