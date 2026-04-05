create database if not exists climate;
use climate;

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
    declare a_time datetime default get_mid_announceTime(now);
    declare f_time datetime default cast(
            date_format(date_add(a_time, interval 3 day), '%Y-%m-%d 09:00:00') as datetime
                                    );

    INSERT INTO mid_pop (id, announce_time, effective_time, province_region_code_id, pop)
    WITH RECURSIVE seq AS (
        SELECT 0 AS i
        UNION ALL
        SELECT i + 1 FROM seq WHERE i < 11
    )
    SELECT null,
           a_time,
           DATE_ADD(f_time, INTERVAL s.i * 12 HOUR),
           p.id,
           s.i * 10
    FROM province_region_code p
             CROSS JOIN seq s;
end $$
delimiter ;

delimiter $$
create procedure insert_mid_temp(now datetime)
begin
    declare a_time datetime default get_mid_announceTime(now);
    declare f_time datetime default cast(
            date_format(date_add(a_time, interval 3 day), '%Y-%m-%d 09:00:00') as datetime
                                    );

    INSERT INTO mid_temperature (id, announce_time, effective_time, city_region_code_id, max_temp, min_temp)
    WITH RECURSIVE seq AS (
        SELECT 0 AS i
        UNION ALL
        SELECT i + 1 FROM seq WHERE i < 11
    )
    SELECT null,
           a_time,
           DATE_ADD(f_time, INTERVAL s.i * 12 HOUR),
           c.id,
           s.i,
           s.i
    FROM city_region_code c
             CROSS JOIN seq s;
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
    declare a_time datetime default get_short_land_announce_time(now);
    declare a_hour int default hour(a_time);
    declare f_time datetime;

    if a_hour = 5 or a_hour = 17 then
        set f_time = date_add(a_time, interval 4 hour);
    else
        set f_time = date_add(a_time, interval 10 hour);
    end if;

    INSERT INTO short_land (id, announce_time, effective_time, city_region_code_id, pop, temp, rain_type)
    WITH RECURSIVE seq AS (
        SELECT -1 AS i
        UNION ALL
        SELECT i + 1 FROM seq WHERE i < 8
    )
    SELECT null,
           a_time,
           DATE_ADD(f_time, INTERVAL s.i * 12 HOUR),
           c.id,
           s.i * 10,
           s.i,
           0
    FROM city_region_code c
             CROSS JOIN seq s;
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
    declare latest_at datetime default get_short_grid_announce_time(now);
    declare past_at datetime default date_sub(latest_at, interval 3 hour);

    INSERT INTO short_grid (id, announce_time, effective_time, x, y, pop, temp)
    WITH RECURSIVE seq AS (
        SELECT 1 AS i
        UNION ALL
        SELECT i + 1 FROM seq WHERE i < 26
    ),
                   latest_pop(i, pop) AS (
                       SELECT  1, 0 UNION ALL SELECT  2, 0 UNION ALL SELECT  3,30
                       UNION ALL SELECT  4,70 UNION ALL SELECT  5,80 UNION ALL SELECT  6,70
                       UNION ALL SELECT  7,30 UNION ALL SELECT  8, 0 UNION ALL SELECT  9, 0
                       UNION ALL SELECT 10, 0 UNION ALL SELECT 11, 0 UNION ALL SELECT 12, 0
                       UNION ALL SELECT 13,70 UNION ALL SELECT 14,80 UNION ALL SELECT 15,70
                       UNION ALL SELECT 16, 0 UNION ALL SELECT 17, 0 UNION ALL SELECT 18,60
                       UNION ALL SELECT 19,70 UNION ALL SELECT 20,80 UNION ALL SELECT 21,60
                       UNION ALL SELECT 22, 0 UNION ALL SELECT 23, 0 UNION ALL SELECT 24, 0
                       UNION ALL SELECT 25, 0 UNION ALL SELECT 26, 0
                   ),
                   past_pop(i, pop) AS (
                       SELECT  1, 0 UNION ALL SELECT  2, 0 UNION ALL SELECT  3, 0
                       UNION ALL SELECT  4,20 UNION ALL SELECT  5,70 UNION ALL SELECT  6,80
                       UNION ALL SELECT  7,70 UNION ALL SELECT  8,30 UNION ALL SELECT  9, 0
                       UNION ALL SELECT 10, 0 UNION ALL SELECT 11,60 UNION ALL SELECT 12,70
                       UNION ALL SELECT 13,80 UNION ALL SELECT 14,70 UNION ALL SELECT 15,60
                       UNION ALL SELECT 16, 0 UNION ALL SELECT 17, 0 UNION ALL SELECT 18, 0
                       UNION ALL SELECT 19, 0 UNION ALL SELECT 20,30 UNION ALL SELECT 21,50
                       UNION ALL SELECT 22,70 UNION ALL SELECT 23,80 UNION ALL SELECT 24,70
                       UNION ALL SELECT 25,30 UNION ALL SELECT 26, 0
                   ),
                   cities AS (
                       SELECT DISTINCT x, y FROM city_region_code
                   )
    SELECT null, latest_at, DATE_ADD(latest_at, INTERVAL lp.i HOUR), c.x, c.y, lp.pop, lp.i
    FROM cities c CROSS JOIN latest_pop lp
    UNION ALL
    SELECT null, past_at, DATE_ADD(past_at, INTERVAL pp.i HOUR), c.x, c.y, pp.pop, pp.i
    FROM cities c CROSS JOIN past_pop pp;
end $$
delimiter ;

delimiter $$
create procedure insert_warning(now datetime)
begin
    declare a_time datetime default now;
    declare e_time datetime default date_add(now, interval 1 hour);

    INSERT INTO warning_event (id, warning_region_code, kind, level, prev_level, event_type, announce_time, effective_time)
    VALUES
        (1, 'L1100100', 'RAIN', 'ADVISORY',null, 'NEW', a_time, e_time),
        (2, 'L1100100', 'RAIN', 'WARNING', 'ADVISORY', 'UPGRADED', date_add(a_time, interval 1 hour), date_add(e_time, interval 1 hour)),
        (3, 'L1100200', 'HEAT', 'WARNING', null, 'NEW', a_time, e_time),
        (4, 'L1100200', 'HEAT', 'WARNING', null, 'LIFTED', date_add(a_time, interval 2 hour), date_add(e_time, interval 2 hour));
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
call insert_warning(now);
end $$
delimiter ;