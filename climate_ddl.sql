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
    region_code varchar(20) not null
);

create table mid_temperature_region_code(
	id bigint primary key auto_increment,
    region_code varchar(20) not null
);

insert into mid_pop_region_code values("11B00000");
insert into mid_pop_region_code values("11D10000");
insert into mid_pop_region_code values("11D20000");
insert into mid_pop_region_code values("11C20000");
insert into mid_pop_region_code values("11C10000");
insert into mid_pop_region_code values("11F20000");
insert into mid_pop_region_code values("11F10000");
insert into mid_pop_region_code values("11H10000");
insert into mid_pop_region_code values("11H20000");
insert into mid_pop_region_code values("11G00000");