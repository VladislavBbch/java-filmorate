package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
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

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    private static final String SQL_QUERY_INSERT_FILM = "INSERT INTO FILMS_DIRECTORS(FILM_ID, DIRECTOR_ID) VALUES(?, ?)";
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
    private static final String SQL_QUERY_DELETE_DIRECTOR_FROM_FILM = "DELETE FROM FILMS_DIRECTORS WHERE FILM_ID= :id";
    private static final String SQL_QUERY_INSERT_DIRECTOR = "INSERT INTO FILMS_DIRECTORS (FILM_ID, DIRECTOR_ID)" +
            " VALUES (?,?)";
    private static final String SQL_QUERY_GET_DIRECTOR_BY_FILM_ID = "SELECT ID, NAME FROM DIRECTORS D " +
            "LEFT JOIN FILMS_DIRECTORS FD on FD.DIRECTOR_ID=D.ID WHERE FD.FILM_ID=?";
    private static final String SQL_QUERY_FILM_ID_FROM_FILMS_DIRECTORS = "SELECT FILM_ID FROM FILMS_DIRECTORS " +
            "WHERE DIRECTOR_ID = :id";
    private static final String SQL_QUERY_FILM_ORDER_BY_LIKES = "SELECT F.ID AS FILM_ID, F.NAME AS USER_NAME, " +
            "F.DESCRIPTION, F.RATING_ID, M.NAME AS RATING_NAME, F.RELEASE_DATE, F.DURATION, " +
            "COUNT(FL.USER_ID) AS LIKES FROM FILMS F " +
            "LEFT JOIN RATINGS M ON M.ID = F.RATING_ID " +
            "JOIN FILMS_DIRECTORS D ON F.ID = D.FILM_ID LEFT JOIN LIKES FL ON F.ID = FL.FILM_ID " +
            "WHERE D.DIRECTOR_ID = :id GROUP BY F.ID ORDER BY LIKES DESC";

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
            updateDirectors(film, id, true);
        return getById(id);
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
        updateDirectors(film, film.getId(), false);
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

    private Film mapRowToFilm(SqlRowSet filmRow) {
        return Film.builder()
                .id(filmRow.getLong("ID"))
                .name(filmRow.getString("NAME"))
                .description(filmRow.getString("DESCRIPTION"))
                .releaseDate(filmRow.getDate("RELEASE_DATE").toLocalDate())
                .duration(filmRow.getInt("DURATION"))
                .directors(getDirectorsByFilmId(filmRow.getLong("ID")))
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

    private void updateDirectors(Film film, long id, boolean isNew) {
        if (!isNew) {
            parameterJdbcTemplate.update(SQL_QUERY_DELETE_DIRECTOR_FROM_FILM, Map.of("id", id));
        }
        if (film.getDirectors() != null) {
            List<Director> listOfDirectors = new ArrayList<>(film.getDirectors());
            jdbcTemplate.batchUpdate(SQL_QUERY_INSERT_DIRECTOR, new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, id);
                            ps.setLong(2, listOfDirectors.get(i).getId());
                        }

                        public int getBatchSize() {
                            return listOfDirectors.size();
                        }
                    });
        }
    }

    private Set<Director> getDirectorsByFilmId(long filmId) {
        Set<Director> directors = new HashSet<>(jdbcTemplate.query(
                SQL_QUERY_GET_DIRECTOR_BY_FILM_ID,
                (rs, rowNum) -> new Director(rs.getLong("ID"), rs.getString("NAME")),
                filmId)
        );
        return directors.isEmpty() ? new HashSet<>() : directors;
    }

    public List<Film> getDirectorFilms(long directorId, String sortBy) {
        final Set<Film> sortedByYear = new TreeSet<>(Comparator.comparing(Film::getReleaseDate));
        final List<Film> sortedByLikes = new ArrayList<>();
        switch (sortBy) {
            case ("year"):
                SqlRowSet rowSet = parameterJdbcTemplate.queryForRowSet(SQL_QUERY_FILM_ID_FROM_FILMS_DIRECTORS,
                        Map.of("id", directorId));
                while (rowSet.next()) {
                    Set<Film> filmSet = new HashSet<>();
                    filmSet.add(getById(rowSet.getLong("FILM_ID")));
                    sortedByYear.addAll(filmSet);
                }
                return new ArrayList<>(sortedByYear);

            case ("likes"):
                SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                        SQL_QUERY_FILM_ORDER_BY_LIKES, Map.of("id", directorId));
                while (filmRow.next()) {
                    sortedByLikes.add(getById(filmRow.getLong("FILM_ID")));
                }
                return sortedByLikes;
        }
        throw new InvalidValueException("Get Director Films Exception");
    }
}
