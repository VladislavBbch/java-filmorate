package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.util.Map;

import static java.util.Map.entry;

@Repository
@RequiredArgsConstructor
public class DatabaseLikeRepository implements LikeRepository {
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    private static final String SQL_QUERY_FIND_LIKE = "SELECT * FROM LIKES " +
                                                      "WHERE FILM_ID = :filmId AND USER_ID = :userId";
    private static final String SQL_QUERY_ADD_LIKE = "INSERT INTO LIKES (FILM_ID, USER_ID) VALUES (:filmId, :userId)";

    private static final String SQL_QUERY_DELETE_LIKE = "DELETE FROM LIKES " +
                                                        "WHERE FILM_ID = :filmId AND USER_ID = :userId";

    private static final String SQL_QUERY_ADD_LIKE_REVIEW = "INSERT INTO REVIEWS_LIKES " +
                                                            "(REVIEW_ID, USER_ID, IS_USEFUL) " +
                                                            "VALUES (:id, :userId, :isLike)";
    private static final String SQL_QUERY_DELETE_LIKE_REVIEW = "DELETE FROM REVIEWS_LIKES " +
                                                               "WHERE REVIEW_ID = :id AND USER_ID = :userId AND " +
                                                               "IS_USEFUL = :isLike";

    @Override
    public void addLike(Long filmId, Long userId) {
        SqlRowSet filmRow = findLikeOnFilm(filmId, userId);

        if (!filmRow.next()) {
            parameterJdbcTemplate.update(SQL_QUERY_ADD_LIKE,
                    Map.ofEntries(
                            entry("filmId", filmId),
                            entry("userId", userId)
                    ));
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        SqlRowSet filmRow = findLikeOnFilm(filmId, userId);

        if (filmRow.next()) {
            parameterJdbcTemplate.update(SQL_QUERY_DELETE_LIKE,
                    Map.ofEntries(
                            entry("filmId", filmId),
                            entry("userId", userId)
                    ));
        }
    }

    @Override
    public void addLike(Long id, Long userId, boolean isLike) {
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("id", id);
        map.addValue("userId", userId);
        map.addValue("isLike", isLike);
        parameterJdbcTemplate.update(SQL_QUERY_ADD_LIKE_REVIEW, map);
    }

    @Override
    public void deleteLike(Long id, Long userId, boolean isLike) {
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("id", id);
        map.addValue("userId", userId);
        map.addValue("isLike", isLike);
        parameterJdbcTemplate.update(SQL_QUERY_DELETE_LIKE_REVIEW, map);
    }

    private SqlRowSet findLikeOnFilm(Long filmId, Long userId) {
        return parameterJdbcTemplate.queryForRowSet(SQL_QUERY_FIND_LIKE,
                Map.ofEntries(
                        entry("filmId", filmId),
                        entry("userId", userId)
                ));
    }
}
