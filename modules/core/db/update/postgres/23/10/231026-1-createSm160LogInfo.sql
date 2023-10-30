create table PNRSERVICES_SM160_LOG_INFO (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    --
    login varchar(100) not null,
    pan_id varchar(50) not null,
    channel integer not null,
    is_join_permitted boolean not null,
    data_receive_id integer not null,
    --
    primary key (ID)
);