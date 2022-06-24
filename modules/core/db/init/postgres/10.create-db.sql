-- begin PNRSERVICES_LAST_CLOSED_TERMINALS
create table pnrservices_last_closed_terminals (
    imei varchar(255),
    --
    dt timestamp,
    hermes_id varchar(255),
    mess text,
    --
    primary key (imei)
)^
-- end PNRSERVICES_LAST_CLOSED_TERMINALS
-- begin PNRSERVICES_ZBDROPMODULES
create table pnrservices_zbdropmodules (
    mac varchar(255),
    --
    num varchar(255),
    --
    primary key (mac)
)^
-- end PNRSERVICES_ZBDROPMODULES
-- begin PNRSERVICES_REST_PARAMS
create table pnrservices_rest_params (
    ID uuid,
    --
    add_params text,
    authorization_ varchar(1000),
    authorization_token varchar(1000),
    content_type varchar(255),
    id_type integer not null,
    pwd_token varchar(255),
    url_host varchar(1000),
    url_path varchar(1000),
    url_path_token varchar(1000),
    url_scheme varchar(255),
    usr_token varchar(255),
    --
    primary key (ID)
)^
-- end PNRSERVICES_REST_PARAMS
-- begin PNRSERVICES_SM160_LOG_OPERATIONS
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
)^
-- end PNRSERVICES_SM160_LOG_OPERATIONS
-- begin PNRSERVICES_SM160_LOG_DISCOVERY
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
)^
-- end PNRSERVICES_SM160_LOG_DISCOVERY
-- begin PNRSERVICES_SM160_LOG
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
    START_TIME timestamp,
    END_TIME timestamp,
    --
    primary key (ID)
)^
-- end PNRSERVICES_SM160_LOG
