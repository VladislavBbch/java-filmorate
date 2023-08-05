package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class DatabaseReviewRepository implements ReviewRepository {
    private static final String SQL_QUERY_GET_REVIEW_BY_ID = "SELECT * FROM REVIEWS WHERE ID = :id";
    private static final String SQL_QUERY_CREATE_REVIEW = "INSERT INTO REVIEWS " +
                                                          "(CONTENT, FILM_ID, USER_ID, IS_POSITIVE, USEFULNESS) " +
                                                          "VALUES (:content, :filmId, :userId, :isPositive, 0)";
    private static final String SQL_QUERY_UPDATE_REVIEW = "UPDATE REVIEWS SET " +
                                                          "CONTENT = :content, IS_POSITIVE = :isPositive, " +
                                                          "USEFULNESS = :usefulness WHERE ID = :id";
    private static final String SQL_QUERY_DELETE_REVIEW = "DELETE FROM REVIEWS WHERE ID = :id";
    private static final String SQL_QUERY_GET_ALL_REVIEWS = "SELECT * FROM REVIEWS " +
                                                            "ORDER BY USEFULNESS DESC LIMIT :count";
    private static final String SQL_QUERY_GET_ALL_REVIEWS_FOR_FILM_ID = "SELECT * FROM REVIEWS " +
                                                                        "WHERE FILM_ID = :filmId " +
                                                                        "ORDER BY USEFULNESS DESC LIMIT :count";
    private static final String SQL_QUERY_GET_REVIEW_USEFUL = "SELECT SUM(TEMP.RESULT) AS RESULT FROM " +
                                                              "(SELECT count(USER_ID) AS RESULT FROM REVIEWS_LIKES " +
                                                              "WHERE REVIEW_ID = :id AND IS_USEFUL = TRUE " +
                                                              "UNION ALL " +
                                                              "SELECT -count(USER_ID) AS RESULT FROM REVIEWS_LIKES " +
                                                              "WHERE REVIEW_ID = :id AND IS_USEFUL = FALSE) AS TEMP";

    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    private final ReviewRowMapper reviewMapper;

    @Override
    public Review getById(Long id) {
        final List<Review> reviews = parameterJdbcTemplate.query(SQL_QUERY_GET_REVIEW_BY_ID,
                                                                 Map.of("id", id), reviewMapper);
        if (reviews.size() != 1) {
            return null;
        }

        return reviews.get(0);
    }

    @Override
    public Review create(Review review) {
       return save(review, true);
    }

    @Override
    public Review update(Review review) {
        return save(review, false);
    }

    @Override
    public void delete(Long id) {
        parameterJdbcTemplate.update(SQL_QUERY_DELETE_REVIEW, Map.of("id", id));
    }

    @Override
    public int getUsefulnessReviewById(Long id) {
        final List<Integer> row = parameterJdbcTemplate.query(SQL_QUERY_GET_REVIEW_USEFUL, Map.of("id", id),
                                                               (rs, rowNum) -> rs.getInt("RESULT"));
        if (row.size() > 0) {
            return row.get(0);
        } else {
            return 0;
        }
    }

    @Override
    public List<Review> getAllReviews(Integer count) {
        return parameterJdbcTemplate.query(SQL_QUERY_GET_ALL_REVIEWS, Map.of("count", count), reviewMapper);
    }

    @Override
    public List<Review> getAllReviewsForFilmById(Long filmId, Integer count) {
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("filmId", filmId);
        map.addValue("count", count);
        return parameterJdbcTemplate.query(SQL_QUERY_GET_ALL_REVIEWS_FOR_FILM_ID, map, reviewMapper);
    }

    private Review save(Review review, boolean isNew) {
        String sqlQuery;

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("content", review.getContent());
        map.addValue("isPositive", review.getIsPositive());
        map.addValue("userId", review.getUserId());
        map.addValue("filmId", review.getFilmId());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        if (isNew) {
            sqlQuery = SQL_QUERY_CREATE_REVIEW;

        } else {
            sqlQuery = SQL_QUERY_UPDATE_REVIEW;

            map.addValue("id", review.getReviewId());
            map.addValue("usefulness", review.getUsefulness());
        }

        parameterJdbcTemplate.update(sqlQuery, map, keyHolder);

        return Review.builder()
                .reviewId(Objects.requireNonNull(keyHolder.getKey()).longValue())
                .content(review.getContent())
                .isPositive(review.getIsPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .usefulness(review.getUsefulness())
                .build();
    }

    @Component
    private static class ReviewRowMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet reviewRow, int rowNum) throws SQLException {
            return Review.builder()
                    .reviewId(reviewRow.getLong("ID"))
                    .content(reviewRow.getString("CONTENT"))
                    .isPositive(reviewRow.getBoolean("IS_POSITIVE"))
                    .userId(reviewRow.getLong("USER_ID"))
                    .filmId(reviewRow.getLong("FILM_ID"))
                    .usefulness(reviewRow.getInt("USEFULNESS"))
                    .build();
        }
    }
}
