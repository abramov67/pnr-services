create table PNRSERVICES_SM160_LOG_DISCOVERY (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    LOG_ID uuid,
    MAC varchar(16),
    MESSAGE text,
    --
    primary key (ID)
);