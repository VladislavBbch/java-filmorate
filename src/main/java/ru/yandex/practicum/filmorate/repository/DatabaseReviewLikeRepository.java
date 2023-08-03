package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DatabaseReviewLikeRepository implements LikeRepository {
    private static final String SQL_QUERY_ADD_LIKE_REVIEW = "INSERT INTO REVIEWS_LIKES " +
                                                            "(REVIEW_ID, USER_ID, IS_USEFUL) " +
                                                            "VALUES (:id, :userId, :isLike)";
    private static final String SQL_QUERY_DELETE_LIKE_REVIEW = "DELETE FROM REVIEWS_LIKES " +
            "WHERE REVIEW_ID = :id AND USER_ID = :userId AND " +
            "IS_USEFUL = :isLike";

    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

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
}
