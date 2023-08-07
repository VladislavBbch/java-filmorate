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

import java.util.*;

import static java.util.Map.entry;

@Repository
@RequiredArgsConstructor
@Primary
public class DatabaseFilmRepository implements FilmRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;
    private static final String SQL_QUERY_GET_BY_ID = "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, " +
            "F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
            "FROM FILMS AS F " +
            "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
            "WHERE F.ID = :id";
    private static final String SQL_QUERY_READ_FILMS = "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, " +
            "F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
            "FROM FILMS AS F " +
            "JOIN RATINGS AS R ON F.RATING_ID = R.ID";
    private static final String SQL_QUERY_UPDATE_FILM = "UPDATE FILMS SET NAME = :name, DESCRIPTION = :description, " +
            "RELEASE_DATE = :releaseDate, DURATION = :duration, RATING_ID = :ratingId WHERE FILMS.ID = :id";
    private static final String SQL_QUERY_GET_POPULAR = "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, " +
            "F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME FROM (SELECT FILM_ID, COUNT(*) AS LIKE_COUNT " +
            "FROM LIKES GROUP BY FILM_ID) AS LIKES " +
            "RIGHT JOIN FILMS AS F ON F.ID = LIKES.FILM_ID " +
            "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
            "ORDER BY LIKES.LIKE_COUNT DESC " +
            "LIMIT :count";
    private static final String SQL_QUERY_GET_RATINGS = "SELECT * FROM RATINGS WHERE RATINGS.ID = :id";

    @Override
    @Nullable
    public Film getById(Long id) {
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                SQL_QUERY_GET_BY_ID, Map.of("id", id));
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
        SqlRowSet filmRow = jdbcTemplate.queryForRowSet(SQL_QUERY_READ_FILMS);
        while (filmRow.next()) {
            films.add(mapRowToFilm(filmRow));
        }
        return films;
    }

    @Override
    public Film update(Film film) {
        Long filmId = film.getId();
        parameterJdbcTemplate.update(SQL_QUERY_UPDATE_FILM, Map.ofEntries(
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
    public List<Film> getMostPopularFilms(Integer count) {
        List<Film> films = new ArrayList<>();
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(SQL_QUERY_GET_POPULAR, Map.of("count", count));
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


    private Film mapRowToFilm(SqlRowSet filmRow) {
        return Film.builder()
                .id(filmRow.getLong("ID"))
                .name(filmRow.getString("NAME"))
                .description(filmRow.getString("DESCRIPTION"))
                .releaseDate(filmRow.getDate("RELEASE_DATE").toLocalDate())
                .duration(filmRow.getInt("DURATION"))
                .ratingMpa(new RatingMpa(
                        filmRow.getLong("RATING_ID"), filmRow.getString("RATING_NAME")))
                .build();
    }

    private RatingMpa getRatingInfo(Long ratingId) {
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                SQL_QUERY_GET_RATINGS, Map.of("id", ratingId));
        if (filmRow.next()) {
            return new RatingMpa(filmRow.getLong("ID"), filmRow.getString("NAME"));
        }
        return null;
    }
}
