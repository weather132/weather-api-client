create database if not exists climate;
use climate;

create table short_pop(
	id bigint primary key auto_increment,
    announce_time datetime not null,
    effective_time datetime not null,
    x int not null,
    y int not null,
    pop int not null
);

create table short_temperature(
	id bigint primary key auto_increment,
    announce_time datetime not null,
    effective_time datetime not null,
	x int not null,
    y int not null,
    max_temp int,
    min_temp int
);

create table mid_pop(
	id bigint primary key auto_increment,
	announce_time datetime not null,
    effective_time datetime not null,
    region_code varchar(20) not null,
    pop int not null
);

create table mid_temperature(
	id bigint primary key auto_increment,
    announce_time datetime not null,
    effective_time datetime not null,
    region_code varchar(20) not null,
	max_temp int,
    min_temp int
);

create table mid_pop_region_code(
	id bigint primary key auto_increment,
    region_code varchar(20) not null unique
);

create table mid_city_region_code(
	id bigint primary key auto_increment,
    region_code varchar(20) not null unique,
    x int not null,
    y int not null,
    mid_pop_region_code_id bigint,
    foreign key (mid_pop_region_code_id) references mid_pop_region_code(id) on delete set null
);