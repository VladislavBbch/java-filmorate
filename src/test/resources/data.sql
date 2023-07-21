INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES ('user1@test.ru', 'Пользователь1', 'Имя Фамилия1', '1991-05-05');
INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES ('user2@test.ru', 'Пользователь2', 'Имя Фамилия2', '1992-05-05');
INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES ('user3@test.ru', 'Пользователь3', 'Имя Фамилия3', '1993-05-05');
INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES ('user4@test.ru', 'Пользователь4', 'Имя Фамилия4', '1994-05-05');
INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES ('user5@test.ru', 'Пользователь5', 'Имя Фамилия5', '1995-05-05');
INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES ('user6@test.ru', 'Пользователь6', 'Имя Фамилия6', '1996-05-05');

INSERT INTO FRIENDS (USER_ID, FRIEND_ID, IS_CONFIRMED)
VALUES (1, 3, 'true');
INSERT INTO FRIENDS (USER_ID, FRIEND_ID, IS_CONFIRMED)
VALUES (2, 3, 'false');
INSERT INTO FRIENDS (USER_ID, FRIEND_ID, IS_CONFIRMED)
VALUES (3, 4, 'true');
INSERT INTO FRIENDS (USER_ID, FRIEND_ID, IS_CONFIRMED)
VALUES (3, 5, 'false');


INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
VALUES ('Фильм1', 'Описание1', '2001-05-05', 120, 1);
INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
VALUES ('Фильм2', 'Описание2', '2002-05-05', 120, 2);
INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
VALUES ('Фильм3', 'Описание3', '2003-05-05', 130, 3);
INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
VALUES ('Фильм4', 'Описание4', '2004-05-05', 115, 4);
INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
VALUES ('Фильм5', 'Описание5', '2005-05-05', 120, 5);
INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
VALUES ('Фильм6', 'Описание6', '2006-05-05', 121, 5);

INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (1, 1);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (1, 2);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (1, 3);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (1, 4);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (1, 5);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (2, 2);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (2, 3);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (2, 4);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (2, 5);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (3, 3);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (3, 4);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (3, 5);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (4, 4);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (4, 5);
INSERT INTO LIKES (USER_ID, FILM_ID)
VALUES (5, 5);

INSERT INTO FILMS_GENRES (FILM_ID, GENRE_ID)
VALUES (1, 1);
INSERT INTO FILMS_GENRES (FILM_ID, GENRE_ID)
VALUES (1, 2);