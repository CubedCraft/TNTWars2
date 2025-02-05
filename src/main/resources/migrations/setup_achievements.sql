CREATE TABLE achievements (
    id          int not null AUTO_INCREMENT primary key,
    server_id   int not null,
    title       varchar(255) not null,
    description varchar(255) not null,
    score       int not null,
    type        varchar(32) not null,
    enabled     tinyint(1) not null default 1
);

CREATE TABLE player_achievements (
    id             int not null AUTO_INCREMENT primary key,
    achievement_id int not null,
    user_id        int not null,
    server_id      int not null,
    received       bigint not null,
    FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON UPDATE CASCADE ON DELETE RESTRICT
);

INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Winner I', 'Win a match', 1, 'WINS');
INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Winner II', 'Win 2 matches', 2, 'WINS');
INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Winner III', 'Win 5 matches', 5, 'WINS');

INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Killer I', 'Kill an enemy', 1, 'KILLS');
INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Killer II', 'Kill 5 enemies', 5, 'KILLS');
INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Killer III', 'Kill 10 enemies', 10, 'KILLS');

INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Balancer I', 'Balance the teams 1 time', 1, 'TEAM_BALANCER');
INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Balancer II', 'Balance the teams 2 times', 2, 'TEAM_BALANCER');
INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Balancer III', 'Balance the teams 10 times', 10, 'TEAM_BALANCER');

INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Killstreak I', 'Get a killstreak of 2', 2, 'KILLSTREAK');
INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Killstreak II', 'Get a killstreak of 5', 5, 'KILLSTREAK');
INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Killstreak III', 'Get a killstreak of 10', 10, 'KILLSTREAK');

INSERT INTO achievements (server_id, title, description, score, type)
    VALUES (6, 'Flawless', 'Win a match without any team deaths', 1, 'FLAWLESS');