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


delimiter $$
create function get_mid_announceTime(now datetime) returns datetime deterministic
begin
    declare now_hour integer default hour(now);
    if now_hour >= 18 then
        return cast(date_format(now, '%Y-%m-%d 18:00:00') as datetime);
    elseif now_hour >= 0 and now_hour < 6 then
        return cast(date_format(date_sub(now, interval 1 day), '%Y-%m-%d 18:00:00') as datetime);
    else
        return cast(date_format(now, '%Y-%m-%d 06:00:00') as datetime);
    end if;
end $$
delimiter ;

delimiter $$
create procedure insert_mid_land(now datetime)
begin
    declare announce_time datetime default get_mid_announceTime(now);
    declare first_ef_time datetime default cast(date_format(date_add(announce_time, interval 3 day), '%Y-%m-%d 09:00:00') as datetime);
    declare province_done int default false;
    declare i int default 0;
    declare province_id bigint;

    declare province_cursor cursor for select id from climate.province_region_code;
    declare continue handler for not found set province_done = true;


    open province_cursor;
    province_loop : loop
        fetch province_cursor into province_id;

        if province_done then
            leave province_loop;
        end if;

        set i = 0;
        ef_time_loop : loop
            if i >= 12 then
                leave ef_time_loop;
            end if;

            insert into mid_pop values (null, announce_time,
                                        date_add(first_ef_time, interval i * 12 hour),
                                        province_id,
                                        i * 10);
            set i = i + 1;
        end loop;
    end loop;

    close province_cursor;
end $$
delimiter ;

delimiter $$
create procedure insert_mid_temp(now datetime)
begin
    declare announce_time datetime default get_mid_announceTime(now);
    declare first_ef_time datetime default cast(date_format(date_add(announce_time, interval 3 day), '%Y-%m-%d 09:00:00') as datetime);
    declare i int default 0;
    declare city_done int default false;
    declare city_id bigint;

    declare city_cursor cursor for select id from city_region_code;
    declare continue handler for not found set city_done = true;

    open city_cursor;
    city_loop : loop
        fetch city_cursor into city_id;

        if city_done then
            leave city_loop;
        end if;

        set i = 0;
        ef_time_loop : loop
            if i >= 12 then
                leave ef_time_loop;
            end if;

            insert into mid_temperature values (null,
                                                announce_time,
                                                date_add(first_ef_time, interval i * 12 hour),
                                                city_id,
                                                i,
                                                i);
            set i = i + 1;
        end loop;
    end loop;

    close city_cursor;
end $$
delimiter ;

delimiter $$
create function get_short_land_announce_time(now datetime) returns datetime deterministic
begin
    declare now_hour int default hour(now);

    if now_hour >= 17 then
        return cast(date_format(now, '%Y-%m-%d 17:00:00') as datetime);
    elseif now_hour < 5 then
        return cast(date_format(date_sub(now, interval 1 day), '%Y-%m-%d 17:00:00') as datetime);
    elseif now_hour >= 5 and now_hour < 11 then
        return cast(date_format(now, '%Y-%m-%d 05:00:00') as datetime);
    else
        return cast(date_format(now, '%Y-%m-%d 11:00:00') as datetime);
    end if;
end $$
delimiter ;

delimiter $$
create procedure insert_short_land(now datetime)
begin
    declare announce_time datetime default get_short_land_announce_time(now);
    declare announce_hour int default hour(announce_time);
    declare first_ef_time datetime;
    declare city_id bigint;
    declare city_done int default false;
    declare i int default 0;

    declare city_cursor cursor for select id from city_region_code;
    declare continue handler for not found set city_done = true;

    if announce_hour = 5 or announce_hour = 17 then
        set first_ef_time = date_add(announce_time, interval 4 hour);
    else
        set first_ef_time = date_add(announce_time, interval 10 hour);
    end if;

    open city_cursor;
    city_loop : loop
        fetch city_cursor into city_id;

        if city_done then
            leave city_loop;
        end if;

        set i = -1;
        ef_time_loop : loop
            if i >= 9 then
                leave ef_time_loop;
            end if;

            insert into climate.short_land values (
                                                      null,
                                                      announce_time,
                                                      date_add(first_ef_time, interval 12 * i hour),
                                                      city_id,
                                                      i * 10,
                                                      i,
                                                      0);
            set i = i + 1;
        end loop;
    end loop;

    close city_cursor;
end $$
delimiter ;


delimiter $$
create function get_short_grid_announce_time(now datetime) returns datetime deterministic
begin
    declare now_hour int default hour(now);
    return cast(date_format(date_sub(now, interval (now_hour + 1) mod 3 hour), '%Y-%m-%d %H:00:00') as datetime);
end $$
delimiter ;

delimiter $$
create procedure insert_short_grid(now datetime)
begin
    declare latest_announce_time datetime default get_short_grid_announce_time(now);
    declare past_announce_time datetime default date_sub(latest_announce_time, interval 3 hour);
    declare i int default 1;
    declare num_of_ef_times int default 26;
    declare city_x int;
    declare city_y int;
    declare city_done int default false;

    declare city_cursor cursor for select distinct x, y from city_region_code;
    declare continue handler for not found set city_done = true;

    open city_cursor;
    city_loop : loop
        fetch city_cursor into city_x, city_y;
        if city_done then
            leave city_loop;
        end if;

        set i = 1;
        ef_time_loop : loop
            if i > num_of_ef_times then
                leave ef_time_loop;
            end if;

            insert into short_grid values (
                                              null,
                                              past_announce_time,
                                              date_add(past_announce_time, interval i hour),
                                              city_x,
                                              city_y,
                                              ELT(i, 0,0,30,70,80,70,30,0,0,0,0,0,70,80,70,0,0,60,70,80,60,0,0,0,0,0),
                                              i);
            insert into short_grid values (
                                              null,
                                              latest_announce_time,
                                              date_add(latest_announce_time, interval i hour),
                                              city_x,
                                              city_y,
                                              ELT(i, 0,0,30,70,80,70,30,0,0,0,0,0,70,80,70,0,0,60,70,80,60,0,0,0,0,0),
                                              i);

            set i = i + 1;
        end loop;
    end loop;

    close city_cursor;
end $$
delimiter ;

delimiter $$
create procedure insert_all()
begin
    declare now datetime default now();
    call insert_short_grid(now);
    call insert_short_land(now);
    call insert_mid_land(now);
    call insert_mid_temp(now);
end $$
delimiter ;

call insert_all();