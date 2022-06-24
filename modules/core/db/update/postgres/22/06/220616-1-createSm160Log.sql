create table PNRSERVICES_SM160_LOG (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    IP varchar(15),
    NUM varchar(255),
    PORT integer,
    --
    primary key (ID)
);