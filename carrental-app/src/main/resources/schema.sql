create table if not exists daily_slot (
    branch_id  varchar(64)  not null,
    car_class  varchar(16)  not null,
    slot_date  date         not null,
    capacity   integer      not null,
    allocated  integer      not null,
    primary key (branch_id, car_class, slot_date)
);

create table if not exists blockade (
    id            uuid         primary key,
    branch_id     varchar(64)  not null,
    car_class     varchar(16)  not null,
    period_start  date         not null,
    period_end    date         not null
);

create table if not exists reservation (
    id           uuid         primary key,
    customer_id  uuid         not null,
    branch_id    varchar(64)  not null,
    car_class    varchar(16)  not null,
    period_start date         not null,
    period_end   date         not null,
    pick_up_time time         not null,
    blockade_id  uuid         not null
);

create table if not exists car (
    id         uuid         primary key,
    vin        varchar(32)  not null unique,
    car_class  varchar(16)  not null,
    branch_id  varchar(64)  not null,
    status     varchar(16)  not null
);

create table if not exists car_assignment (
    id              uuid  primary key,
    reservation_id  uuid  not null unique,
    car_id          uuid  not null,
    period_start    date  not null,
    period_end      date  not null
);
