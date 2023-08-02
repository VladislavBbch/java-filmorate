package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;

@Repository
@RequiredArgsConstructor
@Primary
public class DatabaseDirectorRepository implements DirectorRepository {

    private final JdbcTemplate jdbcTemplate;
    @Override
    public Director create(Director director) {
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("DIRECTORS")
                    .usingGeneratedKeyColumns("ID");
            Long id = simpleJdbcInsert.executeAndReturnKey(Map.ofEntries(
                    entry("NAME", director.getName())
            )).longValue();

        return getById(id);
    }

    @Override
    public Director update(Director director) {
        Long id = director.getId();
        try {
            jdbcTemplate.update("UPDATE DIRECTORS SET NAME = ? WHERE ID =" + id, director.getName());
            return director;
        } catch (RuntimeException e) {
            throw new ObjectNotFoundException("Update User Exception");
        }
    }

    @Override
    public List<Director> read() {
        return new ArrayList<>(jdbcTemplate.query("SELECT * FROM DIRECTORS", this::mapRowToDirector));
    }

    @Override
    public Director getById(Long id) {
        String sql = "select * from directors where id=?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToDirector, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ObjectNotFoundException("Get By Id Exception");
        }
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        if (rs.getRow() == 0) {
            throw new ObjectNotFoundException("Director not found");
        }
        return new Director(rs.getLong("id"), rs.getString("name"));
    }
}
