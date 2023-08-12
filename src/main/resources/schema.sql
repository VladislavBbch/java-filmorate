DROP TABLE IF EXISTS FILMS_MARKS, FRIENDS, FILMS_GENRES, REVIEWS_LIKES, RATINGS, FILMS, USERS, GENRES, REVIEWS, DIRECTORS,
    FILMS_DIRECTORS, EVENT_TYPES, OPERATIONS, EVENTS;

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
    FRIEND_ID    INTEGER REFERENCES USERS (ID) ON DELETE CASCADE,
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

CREATE TABLE IF NOT EXISTS FILMS_MARKS
(
    USER_ID INTEGER REFERENCES USERS (ID) ON DELETE CASCADE,
    FILM_ID INTEGER REFERENCES FILMS (ID) ON DELETE CASCADE,
    MARK    INTEGER,
    CONSTRAINT films_marks_pk PRIMARY KEY (USER_ID, FILM_ID),
    CONSTRAINT films_marks_mark CHECK (MARK >= 1 AND MARK <= 10)
);

CREATE TABLE IF NOT EXISTS GENRES
(
    ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME VARCHAR
);

CREATE TABLE IF NOT EXISTS FILMS_GENRES
(
    FILM_ID  INTEGER REFERENCES FILMS (ID) ON DELETE CASCADE,
    GENRE_ID INTEGER REFERENCES GENRES (ID) ON DELETE CASCADE,
    CONSTRAINT films_genres_pk PRIMARY KEY (FILM_ID, GENRE_ID)
);

CREATE TABLE IF NOT EXISTS DIRECTORS
(
    ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME VARCHAR
);

CREATE TABLE IF NOT EXISTS FILMS_DIRECTORS
(
    DIRECTOR_ID INTEGER REFERENCES DIRECTORS (ID) ON DELETE CASCADE,
    FILM_ID     INTEGER REFERENCES FILMS (ID) ON DELETE CASCADE,
    CONSTRAINT films_directors_pk PRIMARY KEY (DIRECTOR_ID, FILM_ID)
);

CREATE TABLE IF NOT EXISTS REVIEWS
(
    ID       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    CONTENT  VARCHAR,
    FILM_ID  INTEGER REFERENCES FILMS (ID) ON DELETE CASCADE,
    USER_ID  INTEGER REFERENCES USERS (ID) ON DELETE CASCADE,
    IS_POSITIVE BOOLEAN,
    USEFULNESS INTEGER
);

CREATE TABLE IF NOT EXISTS REVIEWS_LIKES
(
    REVIEW_ID  INTEGER REFERENCES REVIEWS (ID) ON DELETE CASCADE,
    USER_ID INTEGER REFERENCES USERS (ID) ON DELETE CASCADE,
    IS_USEFUL BOOLEAN,
    CONSTRAINT reviews_likes_pk PRIMARY KEY (REVIEW_ID, USER_ID)
);

CREATE TABLE IF NOT EXISTS EVENT_TYPES (
    ID      INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME    VARCHAR
);

CREATE TABLE IF NOT EXISTS OPERATIONS (
    ID      INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME    VARCHAR
);

CREATE TABLE IF NOT EXISTS EVENTS (
    ID                  INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    USER_ID             INTEGER REFERENCES USERS (ID) ON DELETE CASCADE,
    ENTITY_ID           INTEGER,
    EVENT_TIMESTAMP     TIMESTAMP,
    TYPE_ID       INTEGER REFERENCES EVENT_TYPES (ID),
    OPERATION_ID        INTEGER REFERENCES OPERATIONS (ID)
)

