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
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.util.*;

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
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet("SELECT * FROM FILMS WHERE ID = :id", Map.of("id", id));
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
        addFilmGenres(id, film);
        return film.toBuilder()
                .id(id)
                .ratingMpa(getRatingInfo(film.getRatingMpa().getId()))
                .genres(getFilmGenres(id))
                .build();
    }

    @Override
    public List<Film> read() {
        List<Film> films = new ArrayList<>();
        SqlRowSet filmRow = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS");
        while (filmRow.next()) {
            films.add(mapRowToFilm(filmRow));
        }
        return films;
    }

    @Override
    public Film update(Film film) {
        Long filmId = film.getId();
        parameterJdbcTemplate.update("UPDATE FILMS SET NAME = :name, DESCRIPTION = :description, " +
                        "RELEASE_DATE = :releaseDate, DURATION = :duration, RATING_ID = :ratingId " +
                        "WHERE ID =:id",
                Map.ofEntries(
                        entry("name", film.getName()),
                        entry("description", film.getDescription()),
                        entry("releaseDate", film.getReleaseDate()),
                        entry("duration", film.getDuration()),
                        entry("ratingId", film.getRatingMpa().getId()),
                        entry("id", filmId)
                ));
        parameterJdbcTemplate.update("DELETE FROM FILMS_GENRES WHERE FILM_ID = :id", Map.of("id", filmId));
        addFilmGenres(filmId, film);
        return getById(filmId);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT * FROM LIKES WHERE FILM_ID = :filmId AND USER_ID = :userId",
                Map.ofEntries(
                        entry("filmId", filmId),
                        entry("userId", userId)
                ));
        if (!filmRow.next()) {
            parameterJdbcTemplate.update("INSERT INTO LIKES (FILM_ID, USER_ID) VALUES (:filmId, :userId)",
                    Map.ofEntries(
                            entry("filmId", filmId),
                            entry("userId", userId)
                    ));
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT * FROM LIKES WHERE FILM_ID = :filmId AND USER_ID = :userId",
                Map.ofEntries(
                        entry("filmId", filmId),
                        entry("userId", userId)
                ));
        if (filmRow.next()) {
            parameterJdbcTemplate.update("DELETE FROM LIKES WHERE FILM_ID = :filmId AND USER_ID = :userId",
                    Map.ofEntries(
                            entry("filmId", filmId),
                            entry("userId", userId)
                    ));
        }
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count) {
        List<Film> films = new ArrayList<>();
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID " +
                        "FROM (SELECT FILM_ID, COUNT(*) AS LIKE_COUNT " +
                        "      FROM LIKES " +
                        "      GROUP BY FILM_ID) AS LIKES " +
                        "RIGHT JOIN FILMS AS F ON F.ID = LIKES.FILM_ID " +
                        "ORDER BY LIKES.LIKE_COUNT DESC " +
                        "LIMIT :count", Map.of("count", count));
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
                .ratingMpa(getRatingInfo(filmRow.getLong("RATING_ID")))
                .genres(getFilmGenres(filmRow.getLong("ID")))
                .build();
    }

    private RatingMpa getRatingInfo(Long ratingId) {
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT * FROM RATINGS WHERE ID = :id", Map.of("id", ratingId));
        if (filmRow.next()) {
            return new RatingMpa(filmRow.getLong("ID"), filmRow.getString("NAME"));
        }
        return null;
    }

    private Set<Genre> getFilmGenres(Long filmId) {
        Set<Genre> genres = new HashSet<>();
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT G.ID, G.NAME\n" +
                        "FROM FILMS_GENRES\n" +
                        "JOIN GENRES AS G ON FILMS_GENRES.GENRE_ID = G.ID\n" +
                        "WHERE FILM_ID = :id", Map.of("id", filmId));
        while (filmRow.next()) {
            genres.add(new Genre(filmRow.getLong("ID"), filmRow.getString("NAME")));
        }
        return genres;
    }

    private void addFilmGenres(Long filmId, Film film) {
        if (film.getGenres() == null) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            parameterJdbcTemplate.update("MERGE INTO FILMS_GENRES (FILM_ID, GENRE_ID) VALUES (:filmId, :genreId)", //INSERT - ON CONFLICT DO NOTHING
                    Map.ofEntries(
                            entry("filmId", filmId),
                            entry("genreId", genre.getId())
                    ));
        }
    }
}
