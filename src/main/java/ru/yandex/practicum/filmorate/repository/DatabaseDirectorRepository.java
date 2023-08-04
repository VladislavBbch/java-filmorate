package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

@Repository
@RequiredArgsConstructor
@Primary
public class DatabaseDirectorRepository implements DirectorRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final String SQL_QUERY_GET_ALL = "SELECT * FROM DIRECTORS";
    private static final String SQL_QUERY_GET_DIRECTOR_BY_ID = "SELECT * FROM DIRECTORS WHERE ID = :id";
    private static final String SQL_QUERY_DELETE_DIRECTOR = "DELETE FROM DIRECTORS WHERE ID = :id";
    private static final String SQL_QUERY_UPDATE_DIRECTOR = "UPDATE DIRECTORS SET NAME = :name WHERE ID = :id";

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
        try {
            namedParameterJdbcTemplate.update(
                    SQL_QUERY_UPDATE_DIRECTOR, Map.of("name", director.getName(), "id", director.getId()));
            return director;
        } catch (RuntimeException e) {
            throw new ObjectNotFoundException("Update Director Exception");
        }
    }

    @Override
    public List<Director> read() {
        List<Director> getAll = new ArrayList<>();
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(SQL_QUERY_GET_ALL);
        while (rowSet.next()) {
            getAll.add(mapRowToDirector(rowSet));
        }
        return getAll;
    }

    @Override
    public Director getById(Long id) {
        SqlRowSet rs = namedParameterJdbcTemplate.queryForRowSet(SQL_QUERY_GET_DIRECTOR_BY_ID, Map.of("id", id));
        if (rs.next()) {
            return mapRowToDirector(rs);
        } else throw new ObjectNotFoundException("Director Not Found");
    }

    public void delete(Long id) {
        namedParameterJdbcTemplate.update(SQL_QUERY_DELETE_DIRECTOR, Map.of("id", id));
    }

    private Director mapRowToDirector(SqlRowSet rs) {
        if (rs.getRow() == 0) {
            throw new ObjectNotFoundException("Director not found");
        }
        return new Director(rs.getLong("id"), rs.getString("name"));
    }
}
