create database if not exists climate;
use climate;

create table province_region_code
(
    id bigint primary key auto_increment,
    region_code varchar(20) not null unique
);

create table city_region_code(
    id bigint primary key auto_increment,
    region_code varchar(20) not null unique,
    x int not null,
    y int not null,
    province_region_code_id bigint,
    foreign key (province_region_code_id) references province_region_code(id) on delete set null
);

create table short_grid(
    id bigint primary key auto_increment,
    announce_time datetime not null,
    effective_time datetime not null,
    x int not null,
    y int not null,
    pop int,
    max_temp int,
    min_temp int,
    unique key unique_set(announce_time, effective_time, x, y)
);

create table short_land(
    id bigint primary key auto_increment,
    announce_time datetime not null,
    effective_time datetime not null,
    city_region_code_id bigint not null,
    pop int,
    temp int,
    rain_type int,
    foreign key (city_region_code_id) references city_region_code(id) on delete cascade
);

create table mid_pop(
	id bigint primary key auto_increment,
	announce_time datetime not null,
    effective_time datetime not null,
    province_region_code_id bigint not null,
    pop int not null,
    foreign key (province_region_code_id) references province_region_code(id) on delete cascade
);

create table mid_temperature(
	id bigint primary key auto_increment,
    announce_time datetime not null,
    effective_time datetime not null,
    city_region_code_id bigint not null,
	max_temp int,
    min_temp int,
    foreign key (city_region_code_id) references city_region_code(id) on delete cascade
);