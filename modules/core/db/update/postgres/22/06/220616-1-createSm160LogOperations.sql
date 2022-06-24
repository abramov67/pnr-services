create table PNRSERVICES_SM160_LOG_OPERATIONS (
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
    TYPE varchar(255),
    MESSAGE text,
    STACK_TRACE text,
    --
    primary key (ID)
);