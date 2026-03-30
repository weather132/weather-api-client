create database if not exists climate;
use climate;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS mid_temperature;
DROP TABLE IF EXISTS mid_pop;
DROP TABLE IF EXISTS short_land;
DROP TABLE IF EXISTS short_grid;
DROP TABLE IF EXISTS city_region_code;
DROP TABLE IF EXISTS province_region_code;
SET FOREIGN_KEY_CHECKS = 1;

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


use climate;

insert into province_region_code
values (null, '11B00000'),
       (null, '11D10000'),
       (null, '11D20000'),
       (null, '11C20000'),
       (null, '11C10000'),
       (null, '11F20000'),
       (null, '11F10000'),
       (null, '11H10000'),
       (null, '11H20000'),
       (null, '11G00000');

insert into city_region_code (id, region_code, x, y, province_region_code_id)
values (null, '11B10101', 60, 127, null),
       (null, '11B20201', 55, 124, null),
       (null, '11B20601', 60, 121, null),
       (null, '11B20605', 63, 124, null),
       (null, '11B20602', 59, 123, null),
       (null, '11B10103', 58, 125, null),
       (null, '11B10102', 60, 124, null),
       (null, '11B20606', 62, 114, null),
       (null, '11B20603', 62, 118, null),
       (null, '11B20609', 60, 122, null),
       (null, '11B20612', 64, 119, null),
       (null, '11B20610', 59, 122, null),
       (null, '11B20611', 65, 115, null),
       (null, '11B20604', 57, 119, null),
       (null, '11B20503', 69, 125, null),
       (null, '11B20501', 62, 127, null),
       (null, '11B20502', 64, 128, null),
       (null, '11B20504', 64, 126, null),
       (null, '11B20701', 68, 121, null),
       (null, '11B20703', 71, 121, null),
       (null, '11B20702', 65, 123, null),
       (null, '11B20301', 61, 130, null),
       (null, '11B20302', 57, 128, null),
       (null, '11B20305', 56, 131, null),
       (null, '11B20304', 61, 131, null),
       (null, '11B20401', 61, 134, null),
       (null, '11B20402', 61, 138, null),
       (null, '11B20403', 64, 134, null),
       (null, '11B20404', 69, 133, null),
       (null, '11B20102', 55, 128, null),
       (null, '11B20202', 57, 123, null),
       (null, '11B20204', 57, 125, null),
       (null, '11B20203', 58, 121, null),
       (null, '11H20201', 98, 76, null),
       (null, '11H20101', 102, 84, null),
       (null, '11H20304', 95, 77, null),
       (null, '11H20102', 97, 79, null),
       (null, '11H20301', 90, 77, null),
       (null, '11H20601', 92, 83, null),
       (null, '11H20603', 86, 77, null),
       (null, '11H20604', 87, 83, null),
       (null, '11H20602', 83, 78, null),
       (null, '11H20701', 81, 75, null),
       (null, '11H20704', 74, 73, null),
       (null, '11H20402', 80, 71, null),
       (null, '11H20502', 77, 86, null),
       (null, '11H20503', 81, 84, null),
       (null, '11H20703', 76, 80, null),
       (null, '11H20501', 74, 82, null),
       (null, '11H20401', 87, 68, null),
       (null, '11H20403', 90, 69, null),
       (null, '11H20404', 85, 71, null),
       (null, '11H20405', 77, 68, null),
       (null, '11H10701', 89, 90, null),
       (null, '11H10702', 95, 93, null),
       (null, '11H10703', 91, 90, null),
       (null, '11H10704', 91, 86, null),
       (null, '11H10705', 85, 93, null),
       (null, '11H10601', 80, 96, null),
       (null, '11H10602', 84, 96, null),
       (null, '11H10604', 83, 87, null),
       (null, '11H10605', 83, 91, null),
       (null, '11H10501', 91, 106, null),
       (null, '11H10502', 90, 101, null),
       (null, '11H10503', 96, 103, null),
       (null, '11H10302', 81, 102, null),
       (null, '11H10301', 81, 106, null),
       (null, '11H10303', 86, 107, null),
       (null, '11H10401', 89, 111, null),
       (null, '11H10402', 90, 113, null),
       (null, '11H10403', 97, 108, null),
       (null, '11H10101', 102, 115, null),
       (null, '11H10102', 102, 103, null),
       (null, '11H10201', 102, 94, null),
       (null, '11H10202', 100, 91, null),
       (null, '11F20501', 65, 123, null),
       (null, '11F20503', 56, 71, null),
       (null, '11F20502', 57, 77, null),
       (null, '11F20504', 61, 78, null),
       (null, '11F20505', 61, 72, null),
       (null, '21F20102', 52, 77, null),
       (null, '21F20101', 52, 72, null),
       (null, '21F20801', 50, 67, null),
       (null, '21F20804', 52, 71, null),
       (null, '21F20802', 56, 66, null),
       (null, '21F20201', 48, 59, null),
       (null, '21F20803', 50, 66, null),
       (null, '11F20603', 70, 70, null),
       (null, '11F20402', 73, 70, null),
       (null, '11F20601', 69, 75, null),
       (null, '11F20602', 66, 77, null),
       (null, '11F20301', 57, 56, null),
       (null, '11F20303', 57, 63, null),
       (null, '11F20304', 59, 64, null),
       (null, '11F20302', 54, 61, null),
       (null, '11F20401', 73, 66, null),
       (null, '11F20403', 66, 62, null),
       (null, '11F20404', 62, 66, null),
       (null, '11F10201', 63, 89, null),
       (null, '11F10202', 60, 91, null),
       (null, '21F10501', 56, 92, null),
       (null, '11F10203', 58, 83, null),
       (null, '21F10502', 59, 88, null),
       (null, '11F10401', 68, 80, null),
       (null, '21F10601', 56, 80, null),
       (null, '11F10302', 72, 93, null),
       (null, '21F10602', 56, 87, null),
       (null, '11F10403', 63, 79, null),
       (null, '11F10204', 63, 89, null),
       (null, '11F10402', 66, 84, null),
       (null, '11F10301', 70, 85, null),
       (null, '11F10303', 68, 88, null),
       (null, '11C20401', 67, 100, null),
       (null, '11C20404', 66, 103, null),
       (null, '11C20402', 63, 102, null),
       (null, '11C20602', 62, 97, null),
       (null, '11C20403', 65, 99, null),
       (null, '11C20601', 69, 95, null),
       (null, '11C20301', 63, 110, null),
       (null, '11C20302', 60, 110, null),
       (null, '11C20303', 58, 107, null),
       (null, '11C20101', 51, 110, null),
       (null, '11C20102', 48, 109, null),
       (null, '11C20103', 54, 112, null),
       (null, '11C20104', 55, 106, null),
       (null, '11C20201', 54, 100, null),
       (null, '11C20202', 55, 94, null),
       (null, '11C20502', 57, 103, null),
       (null, '11C20501', 59, 99, null),
       (null, '11C10301', 69, 106, null),
       (null, '11C10304', 71, 110, null),
       (null, '11C10303', 74, 111, null),
       (null, '11C10102', 68, 111, null),
       (null, '11C10101', 76, 114, null),
       (null, '11C10103', 72, 113, null),
       (null, '11C10201', 81, 118, null),
       (null, '11C10202', 84, 115, null),
       (null, '11C10302', 73, 103, null),
       (null, '11C10403', 71, 99, null),
       (null, '11C10402', 74, 97, null),
       (null, '11D10101', 65, 139, null),
       (null, '11D10102', 72, 139, null),
       (null, '11D10201', 80, 138, null),
       (null, '11D10202', 77, 139, null),
       (null, '11D10301', 73, 134, null),
       (null, '11D10302', 75, 130, null),
       (null, '11D10401', 76, 122, null),
       (null, '11D10402', 77, 125, null),
       (null, '11D10501', 86, 119, null),
       (null, '11D10502', 89, 123, null),
       (null, '11D10503', 84, 123, null),
       (null, '11D20401', 87, 141, null),
       (null, '11D20402', 85, 71, null),
       (null, '11D20403', 88, 138, null),
       (null, '11D20501', 92, 131, null),
       (null, '11D20601', 97, 127, null),
       (null, '11D20602', 98, 125, null),
       (null, '11D20301', 95, 119, null),
       (null, '11G00201', 53, 38, null),
       (null, '11G00401', 53, 33, null),
       (null, '11G00601', 28, 8, null);

set SQL_SAFE_UPDATES = 0;
update city_region_code as c
set c.province_region_code_id = (select id
                                 from province_region_code as p
                                 where SUBSTRING(p.region_code, 3, 1) = 'B'
                                 limit 1)
where substring(c.region_code, 3, 1) = 'B';
update city_region_code as c join province_region_code as p on substring(c.region_code, 3, 2) = substring(p.region_code, 3, 2)
set c.province_region_code_id = p.id
where substring(c.region_code, 3, 2) = substring(p.region_code, 3, 2);
set SQL_SAFE_UPDATES = 1;