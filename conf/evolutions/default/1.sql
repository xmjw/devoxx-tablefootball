# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table member (
  id                        bigint not null,
  team_id                   bigint,
  number                    varchar(255),
  last_update               timestamp not null,
  constraint pk_member primary key (id))
;

create table team (
  id                        bigint not null,
  name                      varchar(255),
  playing                   boolean,
  seeking                   boolean,
  temp_score                integer,
  wins                      integer,
  loses                     integer,
  playing_against           bigint,
  last_update               timestamp not null,
  constraint pk_team primary key (id))
;

create sequence member_seq;

create sequence team_seq;

alter table member add constraint fk_member_team_1 foreign key (team_id) references team (id);
create index ix_member_team_1 on member (team_id);



# --- !Downs

drop table if exists member cascade;

drop table if exists team cascade;

drop sequence if exists member_seq;

drop sequence if exists team_seq;

