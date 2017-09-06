# --- !Ups

create table access_key (
  primary_key               bigint auto_increment not null,
  device_id                 bigint,
  access_key                varchar(255),
  opq_device_primary_key    bigint,
  constraint pk_access_key primary key (primary_key))
;

create table event (
  primary_key               bigint auto_increment not null,
  timestamp                 bigint,
  event_type                integer,
  frequency                 double,
  voltage                   double,
  duration                  bigint,
  access_key_primary_key    bigint,
  location_primary_key      bigint,
  event_data_primary_key    bigint,
  constraint ck_event_event_type check (event_type in (0,1,2,3)),
  constraint pk_event primary key (primary_key))
;

create table event_data (
  primary_key               bigint auto_increment not null,
  waveform                  MEDIUMTEXT,
  event_primary_key         bigint,
  constraint pk_event_data primary key (primary_key))
;

create table location (
  primary_key               bigint auto_increment not null,
  grid_id                   varchar(255),
  grid_scale                double,
  grid_row                  integer,
  grid_col                  integer,
  north_east_latitude       double,
  north_east_longitude      double,
  south_west_latitude       double,
  south_west_longitude      double,
  constraint pk_location primary key (primary_key))
;

create table opq_device (
  primary_key               bigint auto_increment not null,
  device_id                 bigint,
  description               varchar(255),
  sharing_data              tinyint(1) default 0,
  last_heartbeat            bigint,
  access_key_primary_key    bigint,
  location_primary_key      bigint,
  constraint pk_opq_device primary key (primary_key))
;

create table person (
  primary_key               bigint auto_increment not null,
  first_name                varchar(255),
  last_name                 varchar(255),
  email                     varchar(255),
  password                  varchar(255),
  password_hash             varbinary(255),
  password_salt             varbinary(255),
  alert_email               varchar(255),
  sms_carrier               integer,
  sms_number                varchar(255),
  constraint ck_person_sms_carrier check (sms_carrier in (0,1,2,3,4,5,6,7,8,9,10)),
  constraint pk_person primary key (primary_key))
;


create table access_key_person (
  access_key_primary_key         bigint not null,
  person_primary_key             bigint not null,
  constraint pk_access_key_person primary key (access_key_primary_key, person_primary_key))
;
alter table access_key add constraint fk_access_key_opqDevice_1 foreign key (opq_device_primary_key) references opq_device (primary_key) on delete restrict on update restrict;
create index ix_access_key_opqDevice_1 on access_key (opq_device_primary_key);
alter table event add constraint fk_event_accessKey_2 foreign key (access_key_primary_key) references access_key (primary_key) on delete restrict on update restrict;
create index ix_event_accessKey_2 on event (access_key_primary_key);
alter table event add constraint fk_event_location_3 foreign key (location_primary_key) references location (primary_key) on delete restrict on update restrict;
create index ix_event_location_3 on event (location_primary_key);
alter table event add constraint fk_event_eventData_4 foreign key (event_data_primary_key) references event_data (primary_key) on delete restrict on update restrict;
create index ix_event_eventData_4 on event (event_data_primary_key);
alter table event_data add constraint fk_event_data_event_5 foreign key (event_primary_key) references event (primary_key) on delete restrict on update restrict;
create index ix_event_data_event_5 on event_data (event_primary_key);
alter table opq_device add constraint fk_opq_device_accessKey_6 foreign key (access_key_primary_key) references access_key (primary_key) on delete restrict on update restrict;
create index ix_opq_device_accessKey_6 on opq_device (access_key_primary_key);
alter table opq_device add constraint fk_opq_device_location_7 foreign key (location_primary_key) references location (primary_key) on delete restrict on update restrict;
create index ix_opq_device_location_7 on opq_device (location_primary_key);



alter table access_key_person add constraint fk_access_key_person_access_key_01 foreign key (access_key_primary_key) references access_key (primary_key) on delete restrict on update restrict;

alter table access_key_person add constraint fk_access_key_person_person_02 foreign key (person_primary_key) references person (primary_key) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table access_key;

drop table access_key_person;

drop table event;

drop table event_data;

drop table location;

drop table opq_device;

drop table person;

SET FOREIGN_KEY_CHECKS=1;

