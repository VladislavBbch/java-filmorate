INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES ('user1@test.ru', 'Пользователь1', 'Имя Фамилия1', '1991-05-05'),
       ('user2@test.ru', 'Пользователь2', 'Имя Фамилия2', '1992-05-05'),
       ('user3@test.ru', 'Пользователь3', 'Имя Фамилия3', '1993-05-05'),
       ('user4@test.ru', 'Пользователь4', 'Имя Фамилия4', '1994-05-05'),
       ('user5@test.ru', 'Пользователь5', 'Имя Фамилия5', '1995-05-05'),
       ('user6@test.ru', 'Пользователь6', 'Имя Фамилия6', '1996-05-05');

INSERT INTO FRIENDS (USER_ID, FRIEND_ID, IS_CONFIRMED)
VALUES (1, 3, 'true'),
       (2, 3, 'false'),
       (3, 4, 'true'),
       (3, 5, 'false');


INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
VALUES ('Фильм1', 'Описание1', '2001-05-05', 120, 1),
       ('Фильм2', 'Описание2', '2002-05-05', 120, 2),
       ('Фильм3', 'Описание3', '2003-05-05', 130, 3),
       ('Фильм4', 'Описание4', '2004-05-05', 115, 4),
       ('Фильм5', 'Описание5', '2005-05-05', 120, 5),
       ('Фильм6', 'Описание6', '2006-05-05', 121, 5);

INSERT INTO FILMS_MARKS (USER_ID, FILM_ID, MARK)
VALUES (1, 1, 6),
       (1, 2, 7),
       (1, 3, 8),
       (1, 4, 9),
       (1, 5, 10),
       (2, 2, 7),
       (2, 3, 8),
       (2, 4, 9),
       (2, 5, 10),
       (3, 3, 8),
       (3, 4, 9),
       (3, 5, 10),
       (4, 4, 9),
       (4, 5, 10),
       (5, 5, 10);

INSERT INTO FILMS_GENRES (FILM_ID, GENRE_ID)
VALUES (1, 1),
       (1, 2);