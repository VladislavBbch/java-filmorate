package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DatabaseGenreRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    public List<Genre> read() {
        List<Genre> genres = new ArrayList<>();
        SqlRowSet genreRow = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES");
        while (genreRow.next()) {
            genres.add(new Genre(genreRow.getLong("ID"), genreRow.getString("NAME")));
        }
        return genres;
    }

    @Nullable
    public Genre getById(Long id) {
        SqlRowSet genreRow = parameterJdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE ID = :id", Map.of("id", id));
        if (genreRow.next()) {
            return new Genre(genreRow.getLong("ID"), genreRow.getString("NAME"));
        }
        return null;
    }

    @Nullable
    public List<Genre> getByIds(List<Long> ids) {
        List<Genre> genres = new ArrayList<>();
        SqlParameterSource mapIds = new MapSqlParameterSource("ids", ids);
        SqlRowSet genreRow = parameterJdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE ID IN (:ids)", mapIds);
        while (genreRow.next()) {
            genres.add(new Genre(genreRow.getLong("ID"), genreRow.getString("NAME")));
        }
        return genres;
    }
}
