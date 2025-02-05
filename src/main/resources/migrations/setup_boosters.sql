CREATE TABLE player_boosters (
    id int not null AUTO_INCREMENT,
    user_id int not null,
    server varchar(32) not null,
    PRIMARY KEY (id)
);