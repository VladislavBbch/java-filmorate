package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@Repository
@RequiredArgsConstructor
public class DatabaseDirectorRepository implements DirectorRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    @Override
    public Director create(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("DIRECTORS")
                .usingGeneratedKeyColumns("ID");
        Long id = simpleJdbcInsert.executeAndReturnKey(Map.ofEntries(
                entry("NAME", director.getName())
        )).longValue();
        return Director.builder()
                .id(id)
                .name(director.getName())
                .build();
    }

    @Override
    public Director update(Director director) {
        parameterJdbcTemplate.update(
                "UPDATE DIRECTORS SET NAME = :name WHERE ID = :id",
                Map.of("name", director.getName(), "id", director.getId()));
        return director;
    }

    @Override
    public List<Director> read() {
        List<Director> directors = new ArrayList<>();
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT * FROM DIRECTORS");
        while (rowSet.next()) {
            directors.add(mapRowToDirector(rowSet));
        }
        return directors;
    }

    @Override
    public Director getById(Long id) {
        SqlRowSet rs = parameterJdbcTemplate.queryForRowSet("SELECT * FROM DIRECTORS WHERE ID = :id", Map.of("id", id));
        if (rs.next()) {
            return mapRowToDirector(rs);
        } else return null;
    }

    @Override
    public void delete(Long id) {
        parameterJdbcTemplate.update("DELETE FROM DIRECTORS WHERE ID = :id", Map.of("id", id));
    }

    @Override
    public Set<Director> updateDirectors(Film film, long id, boolean isNew) {
        Set<Director> directors = new HashSet<>();
        if (!isNew) {
            parameterJdbcTemplate.update("DELETE FROM FILMS_DIRECTORS WHERE FILM_ID= :id", Map.of("id", id));
        }
        if (film.getDirectors() != null) {
            List<Director> listOfDirectors = new ArrayList<>(film.getDirectors());
            jdbcTemplate.batchUpdate(
                    "INSERT INTO FILMS_DIRECTORS (FILM_ID, DIRECTOR_ID) VALUES (?,?)",
                    new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, id);
                            ps.setLong(2, listOfDirectors.get(i).getId());
                        }

                        public int getBatchSize() {
                            return listOfDirectors.size();
                        }
                    });
            return getDirectorsByFilmId(id);
        }
        return directors;
    }

    @Override
    public List<Film> getDirectorFilms(Long directorId, String sortBy) {
        final List<Film> directorFilms = new ArrayList<>();
        SqlRowSet filmRow;
        if (sortBy.equals("year")) {
            filmRow = parameterJdbcTemplate.queryForRowSet(
                    "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, " +
                            "F.DURATION, F.RATING_ID, R.NAME AS RATING_NAME " +
                            "FROM FILMS AS F " +
                            "JOIN RATINGS AS R ON F.RATING_ID = R.ID " +
                            "JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID " +
                            "WHERE FD.DIRECTOR_ID = :id " +
                            "ORDER BY RELEASE_DATE",
                    Map.of("id", directorId));
        } else {
            filmRow = parameterJdbcTemplate.queryForRowSet(
                    "SELECT F.ID AS FILM_ID, F.NAME AS USER_NAME, F.DESCRIPTION, F.RATING_ID, " +
                            "M.NAME AS RATING_NAME, F.RELEASE_DATE, F.DURATION, AVG(FM.MARK) AS AVG_MARK " +
                            "FROM FILMS F " +
                            "LEFT JOIN RATINGS M ON M.ID = F.RATING_ID " +
                            "JOIN FILMS_DIRECTORS D ON F.ID = D.FILM_ID " +
                            "LEFT JOIN FILMS_MARKS FM ON F.ID = FM.FILM_ID " +
                            "WHERE D.DIRECTOR_ID = :id " +
                            "GROUP BY F.ID " +
                            "ORDER BY AVG_MARK DESC",
                    Map.of("id", directorId));
        }
        while (filmRow.next()) {
            directorFilms.add(mapRowToFilm(filmRow));
        }
        return directorFilms;
    }

    @Override
    public Set<Director> getDirectorsByFilmId(long filmId) {
        Set<Director> directors = new HashSet<>();
        SqlRowSet rs = parameterJdbcTemplate.queryForRowSet(
                "SELECT ID, NAME " +
                        "FROM DIRECTORS D " +
                        "LEFT JOIN FILMS_DIRECTORS FD on FD.DIRECTOR_ID=D.ID " +
                        "WHERE FD.FILM_ID= :id",
                Map.of("id", filmId));
        while (rs.next()) {
            directors.add(mapRowToDirector(rs));
        }
        return directors.isEmpty() ? new HashSet<>() : directors;
    }

    @Override
    public void enrichFilmDirectors(List<Film> films) {
        List<Long> ids = films.stream().map(Film::getId).collect(Collectors.toList());
        SqlParameterSource idsMap = new MapSqlParameterSource("ids", ids);
        SqlRowSet rs = parameterJdbcTemplate.queryForRowSet(
                "SELECT FD.FILM_ID, D.ID, D.NAME " +
                        "FROM DIRECTORS AS D " +
                        "JOIN FILMS_DIRECTORS AS FD ON D.ID = FD.DIRECTOR_ID " +
                        "WHERE FILM_ID IN (:ids)",
                idsMap);

        if (rs.next()) {
            Map<Long, Film> filmsMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film));
            do {
                Film film = filmsMap.get(rs.getLong("FILM_ID"));
                Set<Director> directors = film.getDirectors();
                if (directors == null) {
                    directors = new HashSet<>();
                    film.setDirectors(directors);
                }
                directors.add(mapRowToDirector(rs));
            } while (rs.next());

            for (Film film : films) {
                Set<Director> directors = filmsMap.get(film.getId()).getDirectors();
                if (directors == null) {
                    film.setDirectors(new HashSet<>());
                } else {
                    film.setDirectors(directors);
                }
            }
        } else {
            for (Film film : films) {
                film.setDirectors(new HashSet<>());
            }
        }
    }

    private Director mapRowToDirector(SqlRowSet rs) {
        return Director.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
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
}
