package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class DatabaseRecommendationRepository implements RecommendationRepository {

    private static final String SQL_QUERY_GET = "SELECT f.*, r.NAME AS RATING_NAME FROM " +
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

    private static final String SQL_QUERY_GET_GENRES = "SELECT G.ID, G.NAME " +
                                                       "FROM FILMS_GENRES " +
                                                       "JOIN GENRES AS G ON FILMS_GENRES.GENRE_ID = G.ID " +
                                                       "WHERE FILM_ID = :id";

    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    private final FilmRowMapper filmMapper;

    @Override
    public List<Film> getByUserIdForLike(Long userId) {
        List<Film> films = parameterJdbcTemplate.query(SQL_QUERY_GET, Map.of("id", userId), filmMapper);
        loadFilmsGenre(films);
        return films;
    }

    private void loadFilmsGenre(List<Film> films) {
        films.forEach(f->f.setGenres(getFilmGenres(f.getId())));
    }

    public Set<Genre> getFilmGenres(Long filmId) {
        Set<Genre> genres = new HashSet<>();
        SqlRowSet filmRow = parameterJdbcTemplate.queryForRowSet(SQL_QUERY_GET_GENRES, Map.of("id", filmId));
        while (filmRow.next()) {
            genres.add(new Genre(filmRow.getLong("ID"), filmRow.getString("NAME")));
        }
        return genres;
    }

    @Component
    private static class FilmRowMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet filmRow, int rowNum) throws SQLException {
            return Film.builder()
                    .id(filmRow.getLong("ID"))
                    .name(filmRow.getString("NAME"))
                    .description(filmRow.getString("DESCRIPTION"))
                    .releaseDate(filmRow.getDate("RELEASE_DATE").toLocalDate())
                    .duration(filmRow.getInt("DURATION"))
                    .ratingMpa(new RatingMpa(filmRow.getLong("RATING_ID"),
                                             filmRow.getString("RATING_NAME")))
                    .build();
        }
    }
}
