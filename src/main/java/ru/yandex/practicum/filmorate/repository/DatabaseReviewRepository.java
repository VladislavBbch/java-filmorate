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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Primary
public class DatabaseReviewRepository implements ReviewRepository {
    private static final String SQL_QUERY_GET_REVIEW_BY_ID = "SELECT * FROM REVIEWS WHERE ID = :id";
    private static final String SQL_QUERY_CREATE_REVIEW = "INSERT INTO REVIEWS " +
                                                          "(CONTENT, FILM_ID, USER_ID, IS_POSITIVE, USEFUL) " +
                                                          "VALUES (:CONTENT, :FILM_ID, :USER_ID, :IS_POSITIVE, 0)";
    private static final String SQL_QUERY_UPDATE_REVIEW = "UPDATE REVIEWS SET " +
                                                          "CONTENT = :CONTENT, FILM_ID = :FILM_ID, " +
                                                          "USER_ID = :USER_ID, IS_POSITIVE = :IS_POSITIVE, " +
                                                          "USEFUL = :USEFUL WHERE ID = :id";
    private static final String SQL_QUERY_DELETE_REVIEW = "DELETE FROM REVIEWS WHERE ID = :id";
    private static final String SQL_QUERY_DELETE_REVIEW_USEFUL = "DELETE FROM USEFUL_REVIEWS where REVIEW_ID = :id";
    private static final String SQL_QUERY_GET_ALL_REVIEWS = "SELECT * FROM REVIEWS " +
                                                            "ORDER BY USEFUL DESC LIMIT :count";
    private static final String SQL_QUERY_GET_ALL_REVIEWS_FOR_FILM_ID = "SELECT * FROM REVIEWS " +
                                                                        "WHERE FILM_ID = :filmId " +
                                                                        "ORDER BY USEFUL DESC LIMIT :count";
    private static final String SQL_QUERY_ADD_LIKE_REVIEW = "INSERT INTO USEFUL_REVIEWS " +
                                                            "(REVIEW_ID, USER_ID, IS_USEFUL) " +
                                                            "VALUES (:id, :userId, :isLike)";
    private static final String SQL_QUERY_DELETE_LIKE_REVIEW = "DELETE FROM USEFUL_REVIEWS " +
                                                               "WHERE ID = :id, USER_ID = :userId, " +
                                                               "IS_USEFUL = :isLike)";

    private static final String SQL_QUERY_GET_REVIEW_USEFUL = "SELECT SUM(TEMP.RESULT) as RESULT FROM " +
                                                              "(SELECT count(USER_ID) AS RESULT FROM USEFUL_REVIEWS " +
                                                              "WHERE REVIEW_ID = :id AND IS_USEFUL = TRUE " +
                                                              "UNION ALL " +
                                                              "SELECT -count(USER_ID) AS RESULT FROM USEFUL_REVIEWS " +
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
    public List<Review> read() {
        //Для задачи отзывов нет функционала чтения всех отзывов без ограничения, ставим заглушку
        //Можно внести изменения в CRUD - read(count)
        return new ArrayList<>();
    }

    @Override
    public Review deleteReview(Review review) {
        parameterJdbcTemplate.update(SQL_QUERY_DELETE_REVIEW, Map.of("id", review.getReviewId()));
        return review;
    }

    @Override
    public void deleteUsefulReviewById(Long id) {
        parameterJdbcTemplate.update(SQL_QUERY_DELETE_REVIEW_USEFUL, Map.of("id", id));
    }

    @Override
    public int getUsefulReviewById(Long id) {
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

    private Review save(Review review, boolean isNew) {
        String sqlQuery;

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("CONTENT", review.getContent());
        map.addValue("IS_POSITIVE", review.getIsPositive());
        map.addValue("USER_ID", review.getUserId());
        map.addValue("FILM_ID", review.getFilmId());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        if (isNew) {
            sqlQuery = SQL_QUERY_CREATE_REVIEW;
        } else {
            sqlQuery = SQL_QUERY_UPDATE_REVIEW;

            map.addValue("id", review.getReviewId());
            map.addValue("USEFUL", review.getUseful());
        }

        parameterJdbcTemplate.update(sqlQuery, map, keyHolder);
        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        return review;
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
                    .useful(reviewRow.getInt("USEFUL"))
                    .build();
        }
    }
}
