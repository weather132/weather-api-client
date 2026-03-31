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

create table warning_region_mapping (
                                        region_code          varchar(16) not null,
                                        warning_region_code  varchar(16) not null,
                                        primary key (region_code, warning_region_code)
);

create table warning_current (
                                 warning_region_code  varchar(16) not null,
                                 kind                 varchar(20) not null,
                                 level                varchar(12) not null,
                                 announce_time        datetime    not null,
                                 effective_time       datetime    not null,
                                 primary key (warning_region_code, kind)
);

create table warning_event (
                               id                   bigint       auto_increment primary key,
                               warning_region_code  varchar(16)  not null,
                               kind                 varchar(20)  not null,
                               level                varchar(12)  not null,
                               prev_level           varchar(12),
                               event_type           varchar(12)  not null,
                               announce_time        datetime     not null,
                               effective_time       datetime     not null,
                               index idx_wrn_reg_kind_id (warning_region_code, kind, id)
);


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

-- =========================================================
-- warning_region_mapping (예보코드 → 특보코드)
-- =========================================================

-- ---------------------------------------------------------
-- 서울 (1:4)
-- ---------------------------------------------------------
insert into warning_region_mapping
values ('11B10101', 'L1100100'), -- 서울동남권
       ('11B10101', 'L1100200'), -- 서울동북권
       ('11B10101', 'L1100300'), -- 서울서남권
       ('11B10101', 'L1100400'), -- 서울서북권
-- ---------------------------------------------------------
-- 경기도
-- ---------------------------------------------------------
       ('11B10102', 'L1010300'), -- 과천
       ('11B10103', 'L1010200'), -- 광명
       ('11B20102', 'L1010700'), -- 김포
       ('11B20201', 'L1010800'), -- 인천
       ('11B20202', 'L1010500'), -- 시흥
       ('11B20203', 'L1010400'), -- 안산
       ('11B20204', 'L1010600'), -- 부천
       ('11B20301', 'L1011700'), -- 의정부
       ('11B20302', 'L1011500'), -- 고양
       ('11B20304', 'L1011600'), -- 양주
       ('11B20305', 'L1011800'), -- 파주
       ('11B20401', 'L1011100'), -- 동두천
       ('11B20402', 'L1011200'), -- 연천
       ('11B20403', 'L1011300'), -- 포천
       ('11B20404', 'L1011400'), -- 가평
       ('11B20501', 'L1012200'), -- 구리
       ('11B20502', 'L1012300'), -- 남양주
       ('11B20503', 'L1013500'), -- 양평
       ('11B20504', 'L1012800'), -- 하남
       ('11B20601', 'L1011900'), -- 수원
       ('11B20602', 'L1012100'), -- 안양
       ('11B20603', 'L1012400'), -- 오산
       ('11B20604', 'L1013200'), -- 화성
       ('11B20605', 'L1012000'), -- 성남
       ('11B20606', 'L1012500'), -- 평택
       ('11B20609', 'L1012700'), -- 의왕
       ('11B20610', 'L1012600'), -- 군포
       ('11B20611', 'L1013100'), -- 안성
       ('11B20612', 'L1012900'), -- 용인
       ('11B20701', 'L1013000'), -- 이천
       ('11B20702', 'L1013400'), -- 광주(경기)
       ('11B20703', 'L1013300'), -- 여주
-- ---------------------------------------------------------
-- 강원 영서 (평지 + 산지 추가 매핑)
-- ---------------------------------------------------------
       ('11D10101', 'L1021300'), -- 철원
       ('11D10102', 'L1021400'), -- 화천
       ('11D10201', 'L1021810'), -- 인제평지
       ('11D10201', 'L1025020'), -- 인제 ← 강원북부산지
       ('11D10202', 'L1021710'), -- 양구평지
       ('11D10301', 'L1021600'), -- 춘천
       ('11D10302', 'L1021510'), -- 홍천평지
       ('11D10302', 'L1026020'), -- 홍천 ← 강원중부산지
       ('11D10401', 'L1021200'), -- 원주
       ('11D10402', 'L1021100'), -- 횡성
       ('11D10402', 'L1026020'), -- 횡성 ← 강원중부산지
       ('11D10501', 'L1020800'), -- 영월
       ('11D10501', 'L1027020'), -- 영월 ← 강원남부산지
       ('11D10502', 'L1021010'), -- 정선평지
       ('11D10502', 'L1027020'), -- 정선 ← 강원남부산지
       ('11D10503', 'L1020910'), -- 평창평지
       ('11D10503', 'L1026020'), -- 평창 ← 강원중부산지
-- ---------------------------------------------------------
-- 강원 영동 (평지 + 산지 추가 매핑)
-- ---------------------------------------------------------
       ('11D20301', 'L1020300'), -- 태백
       ('11D20301', 'L1027020'), -- 태백 ← 강원남부산지
       ('11D20401', 'L1020510'), -- 속초평지
       ('11D20401', 'L1025020'), -- 속초 ← 강원북부산지
       ('11D20402', 'L1020610'), -- 고성(강원)평지
       ('11D20402', 'L1025020'), -- 고성(강원) ← 강원북부산지
       ('11D20403', 'L1020710'), -- 양양평지
       ('11D20403', 'L1025020'), -- 양양 ← 강원북부산지
       ('11D20501', 'L1020110'), -- 강릉평지
       ('11D20601', 'L1020210'), -- 동해평지
       ('11D20602', 'L1020410'), -- 삼척평지
-- ---------------------------------------------------------
-- 충청남도
-- ---------------------------------------------------------
       ('11C20101', 'L1031300'), -- 서산
       ('11C20102', 'L1031100'), -- 태안
       ('11C20103', 'L1031200'), -- 당진
       ('11C20104', 'L1031600'), -- 홍성
       ('11C20201', 'L1031400'), -- 보령
       ('11C20202', 'L1031500'), -- 서천
       ('11C20301', 'L1030200'), -- 천안
       ('11C20302', 'L1030400'), -- 아산
       ('11C20303', 'L1031000'), -- 예산
       ('11C20401', 'L1030100'), -- 대전
       ('11C20402', 'L1030300'), -- 공주
       ('11C20403', 'L1031700'), -- 계룡
       ('11C20404', 'L1031800'), -- 세종
       ('11C20501', 'L1030800'), -- 부여
       ('11C20502', 'L1030900'), -- 청양
       ('11C20601', 'L1030600'), -- 금산
       ('11C20602', 'L1030500'), -- 논산
-- ---------------------------------------------------------
-- 충청북도
-- ---------------------------------------------------------
       ('11C10101', 'L1040800'), -- 충주
       ('11C10102', 'L1041000'), -- 진천
       ('11C10103', 'L1041100'), -- 음성
       ('11C10201', 'L1040900'), -- 제천
       ('11C10202', 'L1041200'), -- 단양
       ('11C10301', 'L1040100'), -- 청주
       ('11C10302', 'L1040300'), -- 보은
       ('11C10303', 'L1040400'), -- 괴산
       ('11C10304', 'L1041300'), -- 증평
       ('11C10402', 'L1040700'), -- 영동
       ('11C10403', 'L1040600'), -- 옥천
-- ---------------------------------------------------------
-- 전라남도
-- ---------------------------------------------------------
       ('11F20501', 'L1050100'), -- 광주(광역시)
       ('11F20502', 'L1050600'), -- 장성
       ('11F20503', 'L1050200'), -- 나주
       ('11F20504', 'L1050300'), -- 담양
       ('11F20505', 'L1050700'), -- 화순
       ('21F20101', 'L1051900'), -- 함평
       ('21F20102', 'L1052000'), -- 영광
       ('21F20201', 'L1052300'), -- 진도
       ('21F20801', 'L1052100'), -- 목포
       ('21F20802', 'L1051700'), -- 영암
       ('21F20803', 'L1052200'), -- 신안
       ('21F20804', 'L1051800'), -- 무안
       ('11F20601', 'L1050500'), -- 구례
       ('11F20602', 'L1050400'), -- 곡성
       ('11F20603', 'L1051200'), -- 순천
       ('11F20301', 'L1051600'), -- 완도
       ('11F20302', 'L1051500'), -- 해남
       ('11F20303', 'L1051400'), -- 강진
       ('11F20304', 'L1051300'), -- 장흥
       ('11F20401', 'L1051000'), -- 여수
       ('11F20402', 'L1051100'), -- 광양
       ('11F20403', 'L1050800'), -- 고흥
       ('11F20404', 'L1050900'), -- 보성
-- ---------------------------------------------------------
-- 전북자치도
-- ---------------------------------------------------------
       ('11F10201', 'L1061300'), -- 전주
       ('11F10202', 'L1061100'), -- 익산
       ('11F10203', 'L1061200'), -- 정읍
       ('11F10204', 'L1060500'), -- 완주
       ('11F10301', 'L1060800'), -- 장수
       ('11F10302', 'L1060700'), -- 무주
       ('11F10303', 'L1060600'), -- 진안
       ('11F10401', 'L1061400'), -- 남원
       ('11F10402', 'L1060900'), -- 임실
       ('11F10403', 'L1061000'), -- 순창
       ('21F10501', 'L1060300'), -- 군산
       ('21F10502', 'L1060400'), -- 김제
       ('21F10601', 'L1060100'), -- 고창
       ('21F10602', 'L1060200'), -- 부안
-- ---------------------------------------------------------
-- 경상북도 (평지 + 산지 추가 매핑)
-- ---------------------------------------------------------
       ('11H10701', 'L1070100'), -- 대구
       ('11H10702', 'L1070400'), -- 영천
       ('11H10703', 'L1070500'), -- 경산
       ('11H10704', 'L1070700'), -- 청도
       ('11H10705', 'L1071000'), -- 칠곡
       ('11H10601', 'L1071100'), -- 김천
       ('11H10602', 'L1070300'), -- 구미
       ('11H10604', 'L1070800'), -- 고령
       ('11H10605', 'L1070900'), -- 성주
       ('11H10501', 'L1071500'), -- 안동
       ('11H10502', 'L1071700'), -- 의성
       ('11H10503', 'L1071800'), -- 청송
       ('11H10301', 'L1071300'), -- 문경
       ('11H10302', 'L1071200'), -- 상주
       ('11H10303', 'L1071400'), -- 예천
       ('11H10401', 'L1071600'), -- 영주
       ('11H10402', 'L1072010'), -- 봉화평지
       ('11H10402', 'L1075020'), -- 봉화 ← 경북북동산지
       ('11H10403', 'L1071910'), -- 영양평지
       ('11H10403', 'L1075020'), -- 영양 ← 경북북동산지
       ('11H10101', 'L1072310'), -- 울진평지
       ('11H10101', 'L1075020'), -- 울진 ← 경북북동산지
       ('11H10102', 'L1072200'), -- 영덕
       ('11H10201', 'L1072400'), -- 포항
       ('11H10202', 'L1072500'), -- 경주
-- ---------------------------------------------------------
-- 경상남도
-- ---------------------------------------------------------
       ('11H20102', 'L1080500'), -- 양산
       ('11H20301', 'L1080600'), -- 창원
       ('11H20304', 'L1080900'), -- 김해
       ('11H20601', 'L1081000'), -- 밀양
       ('11H20602', 'L1081100'), -- 의령
       ('11H20603', 'L1081200'), -- 함안
       ('11H20604', 'L1081300'), -- 창녕
       ('11H20701', 'L1081400'), -- 진주
       ('11H20704', 'L1081500'), -- 하동
       ('11H20402', 'L1082100'), -- 사천
       ('11H20502', 'L1081800'), -- 거창
       ('11H20503', 'L1081900'), -- 합천
       ('11H20703', 'L1081600'), -- 산청
       ('11H20501', 'L1081700'), -- 함양
       ('11H20401', 'L1082000'), -- 통영
       ('11H20403', 'L1082200'), -- 거제
       ('11H20404', 'L1082300'), -- 고성(경남)
       ('11H20405', 'L1082400'), -- 남해
-- ---------------------------------------------------------
-- 부산 (1:3)
-- ---------------------------------------------------------
       ('11H20201', 'L1082500'), -- 부산동부
       ('11H20201', 'L1082600'), -- 부산중부
       ('11H20201', 'L1082700'), -- 부산서부
-- ---------------------------------------------------------
-- 울산 (1:2)
-- ---------------------------------------------------------
       ('11H20101', 'L1082800'), -- 울산동부
       ('11H20101', 'L1082900'), -- 울산서부
-- ---------------------------------------------------------
-- 제주도
-- ---------------------------------------------------------
       ('11G00201', 'L1090700'), -- 제주 → 제주도북부
       ('11G00401', 'L1090900'); -- 서귀포 → 제주도남부


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

call insert_all();