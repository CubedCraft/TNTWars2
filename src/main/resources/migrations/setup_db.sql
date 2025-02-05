CREATE TABLE tntwars_player_stats (
    player_id  int not null,
    coins      int not null default 0,
    kills      int not null default 0,
    deaths     int not null default 0,
    wins       int not null default 0,
    exp        int not null default 0,
    balancer   int not null default 0,
    first_join bigint not null,
    last_join  bigint not null,
    playtime   bigint not null default 0,
    PRIMARY KEY (player_id)
);

CREATE TABLE tntwars_player_settings (
    player_id              int not null,
    offhand_selector       boolean default false,
    rotate_tools           boolean default false,
    rotate_fences          boolean default false,
    friendly_tnt_pushing   boolean default true,
    dispenser_place_assist int default 2,
    PRIMARY KEY (player_id),
    FOREIGN KEY (player_id) REFERENCES tntwars_player_stats(player_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE tntwars_games (
    id          int not null AUTO_INCREMENT,
    map         varchar(32) not null,
    gamemode    varchar(32) not null,
    winner      varchar(10),
    mvp         int,
    start_date  bigint not null,
    finish_date bigint not null,
    PRIMARY KEY (id),
    FOREIGN KEY (mvp) REFERENCES tntwars_player_stats(player_id) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE tntwars_player_games (
    id        int not null AUTO_INCREMENT,
    player_id int not null,
    game_id   int not null,
    kills     int not null,
    deaths    int not null,
    team      varchar(10) not null,
    PRIMARY KEY (id),
    FOREIGN KEY (player_id) REFERENCES tntwars_player_stats(player_id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (game_id) REFERENCES tntwars_games(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE tntwars_active_boosters (
    id         int not null AUTO_INCREMENT,
    player_id  int not null,
    booster_id int not null,
    activated  bigint not null,
    PRIMARY KEY (id),
    FOREIGN KEY (player_id) REFERENCES tntwars_player_stats(player_id) ON UPDATE CASCADE ON DELETE CASCADE
);