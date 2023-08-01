DROP TABLE IF EXISTS USERS,FRIENDS, RATINGS, FILMS, LIKES, GENRES, FILMS_GENRES;

CREATE TABLE IF NOT EXISTS USERS
(
    ID       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    EMAIL    VARCHAR,
    LOGIN    VARCHAR,
    NAME     VARCHAR,
    BIRTHDAY DATE
);

CREATE TABLE IF NOT EXISTS FRIENDS
(
    USER_ID      INTEGER REFERENCES USERS (ID) ON DELETE CASCADE,
    FRIEND_ID    INTEGER REFERENCES USERS (ID) ON DELETE CASCADE ,
    IS_CONFIRMED BOOLEAN,
    CONSTRAINT friends_pk PRIMARY KEY (USER_ID, FRIEND_ID)
);

CREATE TABLE IF NOT EXISTS RATINGS
(
    ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME VARCHAR
);

CREATE TABLE IF NOT EXISTS FILMS
(
    ID           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME         VARCHAR,
    DESCRIPTION  VARCHAR(200),
    RELEASE_DATE DATE,
    DURATION     INTEGER,
    RATING_ID    INTEGER REFERENCES RATINGS (ID)
);

CREATE TABLE IF NOT EXISTS LIKES
(
    USER_ID INTEGER REFERENCES USERS (ID) ON DELETE CASCADE,
    FILM_ID INTEGER REFERENCES FILMS (ID) ON DELETE CASCADE,
    CONSTRAINT likes_pk PRIMARY KEY (USER_ID, FILM_ID)
);

CREATE TABLE IF NOT EXISTS GENRES
(
    ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME VARCHAR
);

CREATE TABLE IF NOT EXISTS FILMS_GENRES
(
    FILM_ID  INTEGER REFERENCES FILMS (ID) ON DELETE CASCADE,
    GENRE_ID INTEGER REFERENCES GENRES (ID),
    CONSTRAINT films_genres_pk PRIMARY KEY (FILM_ID, GENRE_ID)
);
