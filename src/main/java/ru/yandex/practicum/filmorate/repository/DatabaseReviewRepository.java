package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Primary
public class DatabaseReviewRepository implements ReviewRepository {
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    @Override
    public Review getById(Long id) {
        SqlRowSet reviewRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT * FROM REVIEWS WHERE ID = :id",
                Map.of("id", id));
        if (reviewRow.next()) {
            return mapRowToReview(reviewRow);
        }
        return null;
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
        parameterJdbcTemplate.update("DELETE FROM REVIEWS WHERE ID = :id", Map.of("id", id));
    }

    @Override
    public int getUsefulnessReviewById(Long id) {
        final List<Integer> row = parameterJdbcTemplate.query(
                "SELECT SUM(TEMP.RESULT) AS RESULT FROM " +
                        "(SELECT count(USER_ID) AS RESULT FROM REVIEWS_LIKES " +
                        "WHERE REVIEW_ID = :id AND IS_USEFUL = TRUE " +
                        "UNION ALL " +
                        "SELECT -count(USER_ID) AS RESULT FROM REVIEWS_LIKES " +
                        "WHERE REVIEW_ID = :id AND IS_USEFUL = FALSE) AS TEMP",
                Map.of("id", id),
                (rs, rowNum) -> rs.getInt("RESULT"));
        if (row.size() > 0) {
            return row.get(0);
        } else {
            return 0;
        }
    }

    @Override
    public List<Review> getAllReviews(Integer count) {
        List<Review> reviews = new ArrayList<>();
        SqlRowSet reviewRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT * " +
                        "FROM REVIEWS " +
                        "ORDER BY USEFULNESS DESC " +
                        "LIMIT :count",
                Map.of("count", count));
        while (reviewRow.next()) {
            reviews.add(mapRowToReview(reviewRow));
        }
        return reviews;
    }

    @Override
    public List<Review> getAllReviewsForFilmById(Long filmId, Integer count) {
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("filmId", filmId);
        map.addValue("count", count);
        List<Review> reviews = new ArrayList<>();
        SqlRowSet reviewRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT * FROM REVIEWS " +
                        "WHERE FILM_ID = :filmId " +
                        "ORDER BY USEFULNESS DESC LIMIT :count",
                map);
        while (reviewRow.next()) {
            reviews.add(mapRowToReview(reviewRow));
        }
        return reviews;
    }

    private Review save(Review review, boolean isNew) {
        String sqlQuery;

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("content", review.getContent());
        map.addValue("isPositive", review.getIsPositive());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        if (isNew) {
            sqlQuery = "INSERT INTO REVIEWS (CONTENT, FILM_ID, USER_ID, IS_POSITIVE, USEFULNESS) " +
                    "VALUES (:content, :filmId, :userId, :isPositive, 0)";

            map.addValue("userId", review.getUserId());
            map.addValue("filmId", review.getFilmId());

        } else {
            sqlQuery = "UPDATE REVIEWS SET CONTENT = :content, IS_POSITIVE = :isPositive, USEFULNESS = :usefulness " +
                    "WHERE ID = :id";

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

    private Review mapRowToReview(SqlRowSet reviewRow) {
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
