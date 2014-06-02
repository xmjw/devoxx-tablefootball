# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table fixture (
  id                        bigint not null,
  constraint pk_fixture primary key (id))
;

create table game (
  id                        bigint not null,
  constraint pk_game primary key (id))
;

create table member (
  id                        bigint not null,
  name                      varchar(255),
  number                    varchar(255),
  constraint pk_member primary key (id))
;

create table team (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_team primary key (id))
;

create sequence fixture_seq;

create sequence game_seq;

create sequence member_seq;

create sequence team_seq;




# --- !Downs

drop table if exists fixture cascade;

drop table if exists game cascade;

drop table if exists member cascade;

drop table if exists team cascade;

drop sequence if exists fixture_seq;

drop sequence if exists game_seq;

drop sequence if exists member_seq;

drop sequence if exists team_seq;

