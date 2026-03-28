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
    temp int,
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


insert into province_region_code values(null, '11B00000');
insert into province_region_code values(null, '11D10000');
insert into province_region_code values(null, '11D20000');
insert into province_region_code values(null, '11C20000');
insert into province_region_code values(null, '11C10000');

insert into city_region_code values(null, '11B10101', 60, 127, null);
insert into city_region_code values(null, '11B20201', 55, 124, null);
insert into city_region_code values(null, '11B20601', 60, 121, null);
insert into city_region_code values(null, '11B20605', 63, 124, null);
insert into city_region_code values(null, '11B20602', 59, 123, null);
insert into city_region_code values(null, '11B10103', 58, 125, null);
insert into city_region_code values(null, '11B10102', 60, 124, null);
insert into city_region_code values(null, '11B20606', 62, 114, null);