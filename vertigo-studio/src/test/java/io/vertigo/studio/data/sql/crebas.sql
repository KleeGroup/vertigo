-- ============================================================
--   SGBD      		  :  PostgreSql                     
-- ============================================================




-- ============================================================
--   Sequences                                      
-- ============================================================
create sequence SEQ_ATTACHMENT
	start with 1000 cache 20; 

create sequence SEQ_CAR
	start with 1000 cache 20; 

create sequence SEQ_CITY
	start with 1000 cache 20; 

create sequence SEQ_COMMAND
	start with 1000 cache 20; 

create sequence SEQ_COMMAND_TYPE
	start with 1000 cache 20; 

create sequence SEQ_COMMAND_VALIDATION
	start with 1000 cache 20; 

create sequence SEQ_FAMILLE
	start with 1000 cache 20; 


create sequence SEQ_RECORD
	start with 1000 cache 20; 


-- ============================================================
--   Table : ATTACHMENT                                        
-- ============================================================
create table ATTACHMENT
(
    ATT_ID      	 NUMERIC     	not null,
    URL         	 VARCHAR(150)	not null,
    CMD_ID      	 NUMERIC     	,
    constraint PK_ATTACHMENT primary key (ATT_ID)
);

comment on column ATTACHMENT.ATT_ID is
'id';

comment on column ATTACHMENT.URL is
'Url';

comment on column ATTACHMENT.CMD_ID is
'Command';

-- ============================================================
--   Table : CAR                                        
-- ============================================================
create table CAR
(
    ID          	 NUMERIC     	not null,
    MANUFACTURER	 VARCHAR(150)	not null,
    MODEL       	 VARCHAR(2000)	not null,
    DESCRIPTION 	 VARCHAR(2000)	not null,
    YEAR        	 NUMERIC     	not null,
    KILO        	 NUMERIC     	not null,
    PRICE       	 NUMERIC     	not null,
    CONSOMMATION	 NUMERIC(8,2)	not null,
    MTY_CD      	 VARCHAR(500)	,
    FAM_ID      	 NUMERIC     	not null,
    constraint PK_CAR primary key (ID)
);

comment on column CAR.ID is
'identifiant de la voiture';

comment on column CAR.MANUFACTURER is
'Constructeur';

comment on column CAR.MODEL is
'Modèle';

comment on column CAR.DESCRIPTION is
'Descriptif';

comment on column CAR.YEAR is
'Année';

comment on column CAR.KILO is
'Kilométrage';

comment on column CAR.PRICE is
'Prix';

comment on column CAR.CONSOMMATION is
'Consommation';

comment on column CAR.MTY_CD is
'Motor type';

comment on column CAR.FAM_ID is
'Famille';

-- ============================================================
--   Table : CITY                                        
-- ============================================================
create table CITY
(
    CIT_ID      	 NUMERIC     	not null,
    LABEL       	 VARCHAR(2000)	not null,
    POSTAL_CODE 	 VARCHAR(150)	not null,
    constraint PK_CITY primary key (CIT_ID)
);

comment on column CITY.CIT_ID is
'id';

comment on column CITY.LABEL is
'Label';

comment on column CITY.POSTAL_CODE is
'Postal code';

-- ============================================================
--   Table : COMMAND                                        
-- ============================================================
create table COMMAND
(
    CMD_ID      	 NUMERIC     	not null,
    CTY_ID      	 NUMERIC     	,
    CIT_ID      	 NUMERIC     	,
    constraint PK_COMMAND primary key (CMD_ID)
);

comment on column COMMAND.CMD_ID is
'id';

comment on column COMMAND.CTY_ID is
'Command type';

comment on column COMMAND.CIT_ID is
'City';

-- ============================================================
--   Table : COMMAND_TYPE                                        
-- ============================================================
create table COMMAND_TYPE
(
    CTY_ID      	 NUMERIC     	not null,
    LABEL       	 VARCHAR(2000)	not null,
    constraint PK_COMMAND_TYPE primary key (CTY_ID)
);

comment on column COMMAND_TYPE.CTY_ID is
'id';

comment on column COMMAND_TYPE.LABEL is
'Label';

-- ============================================================
--   Table : COMMAND_VALIDATION                                        
-- ============================================================
create table COMMAND_VALIDATION
(
    CVA_ID      	 NUMERIC     	not null,
    SIGNER_NAME 	 VARCHAR(2000)	not null,
    CMD_ID      	 NUMERIC     	,
    constraint PK_COMMAND_VALIDATION primary key (CVA_ID)
);

comment on column COMMAND_VALIDATION.CVA_ID is
'id';

comment on column COMMAND_VALIDATION.SIGNER_NAME is
'Signer name';

comment on column COMMAND_VALIDATION.CMD_ID is
'Command';

-- ============================================================
--   Table : FAMILLE                                        
-- ============================================================
create table FAMILLE
(
    FAM_ID      	 NUMERIC     	not null,
    LIBELLE     	 VARCHAR(500)	,
    constraint PK_FAMILLE primary key (FAM_ID)
);

comment on column FAMILLE.FAM_ID is
'identifiant de la famille';

comment on column FAMILLE.LIBELLE is
'Libelle';

-- ============================================================
--   Table : MOTOR_TYPE                                        
-- ============================================================
create table MOTOR_TYPE
(
    MTY_CD      	 VARCHAR(500)	not null,
    LABEL       	 VARCHAR(2000)	not null,
    constraint PK_MOTOR_TYPE primary key (MTY_CD)
);

comment on column MOTOR_TYPE.MTY_CD is
'id';

comment on column MOTOR_TYPE.LABEL is
'Label';

-- ============================================================
--   Table : RECORD                                        
-- ============================================================
create table RECORD
(
    DOS_ID      	 NUMERIC     	not null,
    REG_ID      	 NUMERIC     	,
    DEP_ID      	 NUMERIC     	,
    COM_ID      	 NUMERIC     	,
    TYP_ID      	 NUMERIC     	not null,
    TITLE       	 VARCHAR(500)	not null,
    AMOUNT      	 NUMERIC(8,2)	not null,
    UTI_ID_OWNER	 NUMERIC     	not null,
    ETA_CD      	 VARCHAR(500)	not null,
    constraint PK_RECORD primary key (DOS_ID)
);

comment on column RECORD.DOS_ID is
'Id';

comment on column RECORD.REG_ID is
'Region';

comment on column RECORD.DEP_ID is
'Departement';

comment on column RECORD.COM_ID is
'Commune';

comment on column RECORD.TYP_ID is
'Record type';

comment on column RECORD.TITLE is
'Title';

comment on column RECORD.AMOUNT is
'Amount';

comment on column RECORD.UTI_ID_OWNER is
'Owner';

comment on column RECORD.ETA_CD is
'State';


alter table COMMAND
	add constraint FK_CIT_CMD_CITY foreign key (CIT_ID)
	references CITY (CIT_ID);

create index CIT_CMD_CITY_FK on COMMAND (CIT_ID asc);

alter table ATTACHMENT
	add constraint FK_CMD_ATT_COMMAND foreign key (CMD_ID)
	references COMMAND (CMD_ID);

create index CMD_ATT_COMMAND_FK on ATTACHMENT (CMD_ID asc);

alter table COMMAND_VALIDATION
	add constraint FK_CMD_CVA_COMMAND foreign key (CMD_ID)
	references COMMAND (CMD_ID);

create index CMD_CVA_COMMAND_FK on COMMAND_VALIDATION (CMD_ID asc);

alter table COMMAND
	add constraint FK_CTY_CMD_COMMAND_TYPE foreign key (CTY_ID)
	references COMMAND_TYPE (CTY_ID);

create index CTY_CMD_COMMAND_TYPE_FK on COMMAND (CTY_ID asc);

alter table CAR
	add constraint FK_FAM_CAR_FAMILLE_FAMILLE foreign key (FAM_ID)
	references FAMILLE (FAM_ID);

create index FAM_CAR_FAMILLE_FAMILLE_FK on CAR (FAM_ID asc);

alter table CAR
	add constraint FK_MTY_CAR_MOTOR_TYPE foreign key (MTY_CD)
	references MOTOR_TYPE (MTY_CD);

create index MTY_CAR_MOTOR_TYPE_FK on CAR (MTY_CD asc);


create table FAM_CAR_LOCATION
(
	FAM_ID      	 NUMERIC     	 not null,
	ID          	 NUMERIC     	 not null,
	constraint PK_FAM_CAR_LOCATION primary key (FAM_ID, ID),
	constraint FK_amCarLocation_FAMILLE 
		foreign key(FAM_ID)
		references FAMILLE (FAM_ID),
	constraint FK_amCarLocation_CAR 
		foreign key(ID)
		references CAR (ID)
);

create index amCarLocation_FAMILLE_FK on FAM_CAR_LOCATION (FAM_ID asc);

create index amCarLocation_CAR_FK on FAM_CAR_LOCATION (ID asc);

