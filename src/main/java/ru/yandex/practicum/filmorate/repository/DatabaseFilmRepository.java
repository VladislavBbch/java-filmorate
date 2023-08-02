package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InvalidValueException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.sql.ResultSet;
import java.sql.SQLException;
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
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
                        "FROM FILMS AS F " +
                        "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
                        "WHERE F.ID = :id", Map.of("id", id));
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
        if (film.getDirectors() != null) {
            film.getDirectors().stream()
                    .map(Director::getId)
                    .distinct()
                    .forEach(integer ->
                            jdbcTemplate.update("INSERT INTO FILMS_DIRECTORS(FILM_ID, DIRECTOR_ID) VALUES(?, ?)",
                                    id,
                                    integer));
        }
        return film.toBuilder()
                .id(id)
                .ratingMpa(getRatingInfo(film.getRatingMpa().getId()))
                .directors(getDirectorsByFilmId(id))
                .build();
    }

    @Override
    public List<Film> read() {
        List<Film> films = new ArrayList<>();
        SqlRowSet filmRow = jdbcTemplate.queryForRowSet(
                "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
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
        parameterJdbcTemplate.update("UPDATE FILMS SET NAME = :name, DESCRIPTION = :description, " +
                        "RELEASE_DATE = :releaseDate, DURATION = :duration, RATING_ID = :ratingId " +
                        "WHERE FILMS.ID = :id",
                Map.ofEntries(
                        entry("name", film.getName()),
                        entry("description", film.getDescription()),
                        entry("releaseDate", film.getReleaseDate()),
                        entry("duration", film.getDuration()),
                        entry("ratingId", film.getRatingMpa().getId()),
                        entry("id", filmId)
                ));
        updateDirectors(film);
        return film;
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count) {
        List<Film> films = new ArrayList<>();
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
                        "FROM (SELECT FILM_ID, COUNT(*) AS LIKE_COUNT " +
                        "      FROM LIKES " +
                        "      GROUP BY FILM_ID) AS LIKES " +
                        "RIGHT JOIN FILMS AS F ON F.ID = LIKES.FILM_ID " +
                        "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
                        "ORDER BY LIKES.LIKE_COUNT DESC " +
                        "LIMIT :count", Map.of("count", count));
        while (filmRow.next()) {
            films.add(mapRowToFilm(filmRow));
        }
        return films;
    }

    private Film mapRowToFilm(SqlRowSet filmRow) {
        Long id = filmRow.getLong("ID");
        String likesSql = "select * from LIKES where film_id = ?";
        List<Integer> usersCollection = jdbcTemplate.query(likesSql, (rs1, rowNum) -> makeFilmsLike(rs1), id);
        return Film.builder()
                .id(id)
                .name(filmRow.getString("NAME"))
                .description(filmRow.getString("DESCRIPTION"))
                .releaseDate(filmRow.getDate("RELEASE_DATE").toLocalDate())
                .duration(filmRow.getInt("DURATION"))
                .directors(getDirectorsByFilmId(filmRow.getLong("ID")))
                .ratingMpa(new RatingMpa(filmRow.getLong("RATING_ID"), filmRow.getString("RATING_NAME")))
                .likes(usersCollection)
                .build();
    }

    private Integer makeFilmsLike(ResultSet rs) throws SQLException {
        return rs.getInt("user_id");
    }

    private RatingMpa getRatingInfo(Long ratingId) {
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT * FROM RATINGS WHERE RATINGS.ID = :id", Map.of("id", ratingId));
        if (filmRow.next()) {
            return new RatingMpa(filmRow.getLong("ID"), filmRow.getString("NAME"));
        }
        return null;
    }

    private void updateDirectors(Film film) {
        String deleteSql = "DELETE FROM FILMS_DIRECTORS WHERE FILM_ID=?";
        String insertSql = "INSERT INTO FILMS_DIRECTORS (FILM_ID, DIRECTOR_ID) VALUES (?,?)";

        jdbcTemplate.update(deleteSql, film.getId());
        if (film.getDirectors() != null) {
            film.getDirectors().stream()
                    .map(Director::getId)
                    .forEach(id -> jdbcTemplate.update(insertSql, film.getId(), id));
        }
    }

    private Set<Director> getDirectorsByFilmId(long filmId) {
        String sql = "SELECT ID, NAME FROM DIRECTORS D " +
                "LEFT JOIN FILMS_DIRECTORS FD on FD.DIRECTOR_ID=D.ID " +
                "where FD.FILM_ID=?";
        Set<Director> directors = new HashSet<>(jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Director(rs.getLong("id"), rs.getString("name")),
                filmId)
        );
        return directors.isEmpty() ? new HashSet<>() : directors;
    }

    public Set<Film> getDirectorFilms(long directorId, String sortBy) {
        Set<Film> sortedByYear = new TreeSet<>(Comparator.comparing(Film::getReleaseDate));
        Set<Film> sortedByLikes = new TreeSet<>(Comparator.comparing(film -> film.getLikes().size()));

        switch (sortBy) {
            case ("year"):
                String selectByYear = "SELECT FILM_ID FROM FILMS_DIRECTORS WHERE DIRECTOR_ID = ?";
                SqlRowSet rowSet = jdbcTemplate.queryForRowSet(selectByYear, directorId);
                while (rowSet.next()) {
                    Set<Film> filmSet = new HashSet<>();
                    filmSet.add(getById(rowSet.getLong("FILM_ID")));
                    sortedByYear.addAll(filmSet);
                }
                return sortedByYear;

            case ("likes"):
                String selectByLikes = "SELECT F.ID AS FILM_ID, F.NAME AS USER_NAME, F.DESCRIPTION, F.RATING_ID, M.NAME AS RATING_NAME, " +
                        "F.RELEASE_DATE, F.DURATION, COUNT(FL.USER_ID) AS LIKES FROM FILMS F " +
                        "LEFT JOIN RATINGS M ON M.ID = F.RATING_ID " +
                        "JOIN FILMS_DIRECTORS D ON F.ID = D.FILM_ID LEFT JOIN LIKES FL ON F.ID = FL.FILM_ID " +
                        "WHERE D.DIRECTOR_ID = ? GROUP BY F.ID ORDER BY LIKES";
                //SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(selectByLikes, Map.of("id", directorId));
                List<Long> list = jdbcTemplate.query(selectByLikes, (rs, rowNum) -> rs.getLong("FILM_ID"), directorId);
                for (Long id : list) {
                    sortedByLikes.add(getById(id));
                }
                return sortedByLikes;
        }
        throw new InvalidValueException("Get Director Films Exception");
    }
}
