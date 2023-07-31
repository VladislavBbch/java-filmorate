package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DatabaseRatingMpaRepository implements RatingMpaRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    @Override
    public List<RatingMpa> read() {
        List<RatingMpa> ratings = new ArrayList<>();
        SqlRowSet ratingMpaRow = jdbcTemplate.queryForRowSet("SELECT * FROM RATINGS");
        while (ratingMpaRow.next()) {
            ratings.add(mapRowToRatingMpa(ratingMpaRow));
        }
        return ratings;
    }

    @Override
    @Nullable
    public RatingMpa getById(Long id) {
        SqlRowSet ratingMpaRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT * FROM RATINGS WHERE ID = :id", Map.of("id", id));
        if (ratingMpaRow.next()) {
            return mapRowToRatingMpa(ratingMpaRow);
        }
        return null;
    }

    private RatingMpa mapRowToRatingMpa(SqlRowSet ratingMpaRow) {
        return new RatingMpa(ratingMpaRow.getLong("ID"), ratingMpaRow.getString("NAME"));
    }
}
