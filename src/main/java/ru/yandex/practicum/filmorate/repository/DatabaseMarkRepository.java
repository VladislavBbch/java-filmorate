package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.util.Map;

import static java.util.Map.entry;

@Repository
@RequiredArgsConstructor
public class DatabaseMarkRepository implements MarkRepository {
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    @Override
    public void addMark(Long filmId, Long userId, Integer mark) {
        SqlRowSet filmRow = findMarkOnFilm(filmId, userId);

        if (!filmRow.next()) {
            parameterJdbcTemplate.update(
                    "INSERT INTO FILMS_MARKS (FILM_ID, USER_ID, MARK) VALUES (:filmId, :userId, :mark)",
                    Map.ofEntries(
                            entry("filmId", filmId),
                            entry("userId", userId),
                            entry("mark", mark)
                    ));
        }
    }

    @Override
    public void deleteMark(Long filmId, Long userId) {
        SqlRowSet filmRow = findMarkOnFilm(filmId, userId);

        if (filmRow.next()) {
            parameterJdbcTemplate.update(
                    "DELETE FROM FILMS_MARKS WHERE FILM_ID = :filmId AND USER_ID = :userId",
                    Map.ofEntries(
                            entry("filmId", filmId),
                            entry("userId", userId)
                    ));
        }
    }

    private SqlRowSet findMarkOnFilm(Long filmId, Long userId) {
        return parameterJdbcTemplate.queryForRowSet(
                "SELECT * FROM FILMS_MARKS WHERE FILM_ID = :filmId AND USER_ID = :userId",
                Map.ofEntries(
                        entry("filmId", filmId),
                        entry("userId", userId)
                ));
    }
}
