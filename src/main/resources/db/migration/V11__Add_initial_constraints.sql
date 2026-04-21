-- Migration: Adding constrains for all tables
-- Version: 11
-- Description: Adding the required constrains for tables

-- Set schema context to temp
SET search_path TO movie_tracker_schema;

alter table if exists confirmation_token add constraint FKah4p1rycwibwm6s9bsyeckq51 foreign key (user_id) references movie_tracker_schema.users;
alter table if exists movie_collection add constraint FK5mnqlk5hbak9wgl1rvhfjeows foreign key (owner_id) references movie_tracker_schema.users;
alter table if exists movie_collections add constraint FKhrnrbir7cfbauoqcw4dywsqkq foreign key (movie_id) references movie_tracker_schema.movies;
alter table if exists movie_collections add constraint FKkofyifayfv9i57es9xvhcg0sq foreign key (collection_id) references movie_tracker_schema.movie_collection;
alter table if exists movie_subscriptions add constraint FKm6ob17kt1wsu2awcwp5ecp10x foreign key (movie_id) references movie_tracker_schema.movies;
alter table if exists movie_subscriptions add constraint FKr0u24qr88i0bpscamww64ru45 foreign key (user_id) references movie_tracker_schema.users;
alter table if exists password_reset_token add constraint FK83nsrttkwkb6ym0anu051mtxn foreign key (user_id) references movie_tracker_schema.users;
alter table if exists users add constraint FKp56c1712k691lhsyewcssf40f foreign key (role_id) references movie_tracker_schema.roles;
alter table if exists watched_movie_history add constraint FK4524d3qtybttq6xq96osfghh3 foreign key (movie_id) references movie_tracker_schema.movies;
alter table if exists watched_movie_history add constraint FKr4xd5c2ey1xqvg8pi48pvh8tc foreign key (user_id) references movie_tracker_schema.users;