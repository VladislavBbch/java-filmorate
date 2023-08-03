package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.util.Map;

import static java.util.Map.entry;

@Repository
@RequiredArgsConstructor
@Primary
public class DatabaseLikeRepository implements LikeRepository {
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    @Override
    public void addLike(Long filmId, Long userId, boolean isLike) {
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
    public void deleteLike(Long filmId, Long userId, boolean isLike) {
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
}
