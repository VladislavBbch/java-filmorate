package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

@Repository
@RequiredArgsConstructor
@Primary
public class DatabaseFilmRepository implements FilmRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    @Override
    @Nullable
    public Film getById(Long id) {
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
                        "FROM FILMS AS F " +
                        "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
                        "WHERE F.ID = :id",
                Map.of("id", id));
        if (filmRow.next()) {
            return mapRowToFilm(filmRow);
        }
        return null;
    }

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("ID");
        Long id = simpleJdbcInsert.executeAndReturnKey(Map.ofEntries(
                entry("NAME", film.getName()),
                entry("DESCRIPTION", film.getDescription()),
                entry("RELEASE_DATE", film.getReleaseDate()),
                entry("DURATION", film.getDuration()),
                entry("RATING_ID", film.getRatingMpa().getId())
        )).longValue();
        return film.toBuilder()
                .id(id)
                .ratingMpa(getRatingInfo(film.getRatingMpa().getId()))
                .build();
    }

    @Override
    public List<Film> read() {
        List<Film> films = new ArrayList<>();
        SqlRowSet filmRow = jdbcTemplate.queryForRowSet(
                "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, " +
                        "F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
                        "FROM FILMS AS F " +
                        "JOIN RATINGS AS R ON F.RATING_ID = R.ID");
        while (filmRow.next()) {
            films.add(mapRowToFilm(filmRow));
        }
        return films;
    }

    @Override
    public Film update(Film film) {
        Long filmId = film.getId();
        parameterJdbcTemplate.update(
                "UPDATE FILMS " +
                        "SET NAME = :name, DESCRIPTION = :description, RELEASE_DATE = :releaseDate, " +
                        "DURATION = :duration, RATING_ID = :ratingId " +
                        "WHERE FILMS.ID = :id",
                Map.ofEntries(
                        entry("name", film.getName()),
                        entry("description", film.getDescription()),
                        entry("releaseDate", film.getReleaseDate()),
                        entry("duration", film.getDuration()),
                        entry("ratingId", film.getRatingMpa().getId()),
                        entry("id", filmId)));
        return film;
    }

    @Override
    public void delete(Long id) {
        parameterJdbcTemplate.update("DELETE FROM FILMS " +
                "WHERE ID = :id", Map.of("id", id));
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count, Long genreId, Integer year) {
        List<Film> films = new ArrayList<>();
        SqlRowSet filmRow;
        if (genreId != null & year != null) {
            filmRow = parameterJdbcTemplate.queryForRowSet(
                    "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, " +
                            "R.NAME AS RATING_NAME, FG.GENRE_ID " +
                            "FROM (SELECT FILM_ID, COUNT(*) AS LIKE_COUNT " +
                            "      FROM LIKES " +
                            "      GROUP BY FILM_ID) AS LIKES " +
                            "RIGHT JOIN FILMS AS F ON F.ID = LIKES.FILM_ID " +
                            "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
                            "JOIN FILMS_GENRES AS FG ON F.ID = FG.FILM_ID " +
                            "WHERE EXTRACT(YEAR FROM F.RELEASE_DATE) = :year AND FG.GENRE_ID = :genreId " +
                            "ORDER BY LIKES.LIKE_COUNT DESC " +
                            "LIMIT :count",
                    Map.ofEntries(
                            entry("count", count),
                            entry("year", year),
                            entry("genreId", genreId)
                    ));
        } else if (genreId != null) {
            filmRow = parameterJdbcTemplate.queryForRowSet(
                    "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, " +
                            "R.NAME AS RATING_NAME, FG.GENRE_ID " +
                            "FROM (SELECT FILM_ID, COUNT(*) AS LIKE_COUNT " +
                            "      FROM LIKES " +
                            "      GROUP BY FILM_ID) AS LIKES " +
                            "RIGHT JOIN FILMS AS F ON F.ID = LIKES.FILM_ID " +
                            "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
                            "JOIN FILMS_GENRES AS FG ON F.ID = FG.FILM_ID " +
                            "WHERE FG.GENRE_ID = :genreId " +
                            "ORDER BY LIKES.LIKE_COUNT DESC " +
                            "LIMIT :count",
                    Map.ofEntries(
                            entry("count", count),
                            entry("genreId", genreId)
                    ));
        } else if (year != null) {
            filmRow = parameterJdbcTemplate.queryForRowSet(
                    "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
                            "FROM (SELECT FILM_ID, COUNT(*) AS LIKE_COUNT " +
                            "      FROM LIKES " +
                            "      GROUP BY FILM_ID) AS LIKES " +
                            "RIGHT JOIN FILMS AS F ON F.ID = LIKES.FILM_ID " +
                            "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
                            "WHERE EXTRACT(YEAR FROM F.RELEASE_DATE) = :year " +
                            "ORDER BY LIKES.LIKE_COUNT DESC " +
                            "LIMIT :count",
                    Map.ofEntries(
                            entry("count", count),
                            entry("year", year)
                    ));
        } else {
            filmRow = parameterJdbcTemplate.queryForRowSet(
                    "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
                            "FROM (SELECT FILM_ID, COUNT(*) AS LIKE_COUNT " +
                            "      FROM LIKES " +
                            "      GROUP BY FILM_ID) AS LIKES " +
                            "RIGHT JOIN FILMS AS F ON F.ID = LIKES.FILM_ID " +
                            "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
                            "ORDER BY LIKES.LIKE_COUNT DESC " +
                            "LIMIT :count", Map.of("count", count));
        }
        while (filmRow.next()) {
            films.add(mapRowToFilm(filmRow));
        }
        return films;
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        List<Film> films = new ArrayList<>();
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
                        "FROM FILMS AS F " +
                        "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
                        "JOIN LIKES AS UL ON F.ID = UL.FILM_ID AND UL.USER_ID = :userId " +
                        "JOIN LIKES AS FL ON F.ID = FL.FILM_ID AND FL.USER_ID = :friendId " +
                        "JOIN (SELECT FILM_ID, COUNT(USER_ID) AS RATE FROM LIKES GROUP BY FILM_ID) " +
                        "AS R ON R.FILM_ID = F.ID " +
                        "ORDER BY RATE DESC", Map.of("userId", userId, "friendId", friendId));
        while (filmRow.next()) {
            films.add(mapRowToFilm(filmRow));
        }
        return films;
    }

    @Override
    public List<Film> getRecommendationFilmByUserIdForLike(Long userId) {
        final String SQL_QUERY_GET = "SELECT f.*, r.NAME AS RATING_NAME FROM " +
                "(SELECT MATCH.*, l.FILM_ID FROM " +
                "(SELECT count(FILM_ID) AS PRIORITY, USER_ID " +
                "FROM LIKES " +
                "WHERE FILM_ID IN (SELECT FILM_ID FROM LIKES WHERE USER_ID = :id) " +
                "AND USER_ID <> :id " +
                "GROUP BY USER_ID " +
                "ORDER BY count(FILM_ID) DESC) AS MATCH " +
                "JOIN LIKES AS l ON MATCH.USER_ID = l.USER_ID " +
                "WHERE l.FILM_ID NOT IN " +
                "(SELECT FILM_ID FROM LIKES WHERE USER_ID = :id) " +
                "LIMIT 1) AS S " +
                "JOIN FILMS f ON S.FILM_ID = f.ID " +
                "LEFT JOIN RATINGS r ON f.RATING_ID = r.ID";

        List<Film> films = new ArrayList<>();
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(SQL_QUERY_GET, Map.of("id", userId));

        while (filmRow.next()) {
            films.add(mapRowToFilm(filmRow));
        }
        return films;
    }

    @Override
    public List<Film> searchFilms(String query, Boolean byDirector, Boolean byTitle) {
        query = "%" + query + "%";
        List<Film> films = new ArrayList<>();
        SqlRowSet filmRow;
        if (byDirector && byTitle) {
            filmRow = parameterJdbcTemplate.queryForRowSet(
                    "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
                            "FROM (SELECT FILM_ID, COUNT(*) AS LIKE_COUNT " +
                            "      FROM LIKES " +
                            "      GROUP BY FILM_ID) AS LIKES " +
                            "RIGHT JOIN FILMS AS F ON F.ID = LIKES.FILM_ID " +
                            "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
                            "LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID " + //LEFT на случай отсутствия режиссеров
                            "LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID " + //LEFT на случай отсутствия режиссеров
                            "WHERE LOWER(D.NAME) LIKE LOWER(:query) OR LOWER(F.NAME) LIKE LOWER(:query) " +
                            "ORDER BY LIKES.LIKE_COUNT DESC ",
                    Map.of("query", query));
        } else if (byDirector) {
            filmRow = parameterJdbcTemplate.queryForRowSet(
                    "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
                            "FROM (SELECT FILM_ID, COUNT(*) AS LIKE_COUNT " +
                            "      FROM LIKES " +
                            "      GROUP BY FILM_ID) AS LIKES " +
                            "RIGHT JOIN FILMS AS F ON F.ID = LIKES.FILM_ID " +
                            "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
                            "JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID " +
                            "JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID " +
                            "WHERE LOWER(D.NAME) LIKE LOWER(:query) " +
                            "ORDER BY LIKES.LIKE_COUNT DESC ",
                    Map.of("query", query));
        } else {
            filmRow = parameterJdbcTemplate.queryForRowSet(
                    "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
                            "FROM (SELECT FILM_ID, COUNT(*) AS LIKE_COUNT " +
                            "      FROM LIKES " +
                            "      GROUP BY FILM_ID) AS LIKES " +
                            "RIGHT JOIN FILMS AS F ON F.ID = LIKES.FILM_ID " +
                            "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
                            "WHERE LOWER(F.NAME) LIKE LOWER(:query) " +
                            "ORDER BY LIKES.LIKE_COUNT DESC ",
                    Map.of("query", query));
        }
        while (filmRow.next()) {
            films.add(mapRowToFilm(filmRow));
        }
        return films;
    }

    private Film mapRowToFilm(SqlRowSet filmRow) {
        return Film.builder()
                .id(filmRow.getLong("ID"))
                .name(filmRow.getString("NAME"))
                .description(filmRow.getString("DESCRIPTION"))
                .releaseDate(filmRow.getDate("RELEASE_DATE").toLocalDate())
                .duration(filmRow.getInt("DURATION"))
                .ratingMpa(new RatingMpa(filmRow.getLong("RATING_ID"), filmRow.getString("RATING_NAME")))
                .build();
    }

    private RatingMpa getRatingInfo(Long ratingId) {
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT * FROM RATINGS WHERE RATINGS.ID = :id",
                Map.of("id", ratingId));
        if (filmRow.next()) {
            return new RatingMpa(filmRow.getLong("ID"), filmRow.getString("NAME"));
        }
        return null;
    }
}