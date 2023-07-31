package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DatabaseGenreRepository implements GenreRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    @Override
    public List<Genre> read() {
        List<Genre> genres = new ArrayList<>();
        SqlRowSet genreRow = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES");
        while (genreRow.next()) {
            genres.add(mapRowToGenre(genreRow));
        }
        return genres;
    }

    @Override
    @Nullable
    public Genre getById(Long id) {
        SqlRowSet genreRow = parameterJdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE ID = :id", Map.of("id", id));
        if (genreRow.next()) {
            return mapRowToGenre(genreRow);
        }
        return null;
    }

    @Override
    @Nullable
    public List<Genre> getByIds(List<Long> ids) {
        List<Genre> genres = new ArrayList<>();
        SqlParameterSource idsMap = new MapSqlParameterSource("ids", ids);
        SqlRowSet genreRow = parameterJdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE ID IN (:ids)", idsMap);
        while (genreRow.next()) {
            genres.add(mapRowToGenre(genreRow));
        }
        return genres;
    }

    @Override
    public void addFilmGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().size() == 0) {
            return;
        }
        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbcTemplate.batchUpdate("MERGE INTO FILMS_GENRES (FILM_ID, GENRE_ID) VALUES (?, ?)", //INSERT - ON CONFLICT DO NOTHING
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, genres.get(i).getId());
                    }

                    public int getBatchSize() {
                        return genres.size();
                    }
                });
    }

    @Override
    public void deleteFilmGenres(Long filmId) {
        parameterJdbcTemplate.update("DELETE FROM FILMS_GENRES WHERE FILM_ID = :id", Map.of("id", filmId));
    }

    @Override
    public Set<Genre> getFilmGenres(Long filmId) {
        Set<Genre> genres = new HashSet<>();
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT G.ID, G.NAME " +
                        "FROM FILMS_GENRES " +
                        "JOIN GENRES AS G ON FILMS_GENRES.GENRE_ID = G.ID " +
                        "WHERE FILM_ID = :id", Map.of("id", filmId));
        while (filmRow.next()) {
            genres.add(new Genre(filmRow.getLong("ID"), filmRow.getString("NAME")));
        }
        return genres;
    }

    @Override
    public List<Film> enrichFilmsByGenres(List<Film> films) {
        if (films == null || films.size() == 0) {
            return films;
        }
        List<Long> ids = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());
        SqlParameterSource idsMap = new MapSqlParameterSource("ids", ids);
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT FG.FILM_ID, G.ID AS GENRE_ID, G.NAME AS GENRE_NAME " +
                        "FROM FILMS_GENRES AS FG " +
                        "JOIN GENRES AS G ON FG.GENRE_ID = G.ID " +
                        "WHERE FG.FILM_ID IN (:ids)", idsMap);

        if (filmRow.next()) {
            Map<Long, Film> filmsMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film));
            do {
                Film film = filmsMap.get(filmRow.getLong("FILM_ID"));
                Set<Genre> genres = film.getGenres();
                if (genres == null) {
                    genres = new HashSet<>();
                }
                genres.add(new Genre(filmRow.getLong("GENRE_ID"), filmRow.getString("GENRE_NAME")));
                film.setGenres(genres);
            } while (filmRow.next());
            films = new ArrayList<>(filmsMap.values());
        }
        for (Film film : films) {
            if (film.getGenres() == null) {
                film.setGenres(new HashSet<>());
            }
        }
        return films;
    }

    private Genre mapRowToGenre(SqlRowSet genreRow) {
        return new Genre(genreRow.getLong("ID"), genreRow.getString("NAME"));
    }
}
