package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

@Repository
@RequiredArgsConstructor
@Primary
public class DatabaseUserRepository implements UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    @Override
    @Nullable
    public User getById(Long id) {
        SqlRowSet userRow = parameterJdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE ID = :id", Map.of("id", id));
        if (userRow.next()) {
            return mapRowToUser(userRow);
        }
        return null;
    }

    @Override
    public User create(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("ID");
        Long id = simpleJdbcInsert.executeAndReturnKey(Map.ofEntries(
                entry("EMAIL", user.getEmail()),
                entry("LOGIN", user.getLogin()),
                entry("NAME", user.getName()),
                entry("BIRTHDAY", user.getBirthday())
        )).longValue();
        return user.toBuilder()
                .id(id)
                .build();
    }

    @Override
    public List<User> read() {
        List<User> users = new ArrayList<>();
        SqlRowSet userRow = jdbcTemplate.queryForRowSet("SELECT * FROM USERS");
        while (userRow.next()) {
            users.add(mapRowToUser(userRow));
        }
        return users;
    }

    @Override
    public User update(User user) {
        parameterJdbcTemplate.update(
                "UPDATE USERS SET EMAIL = :email, LOGIN = :login, NAME = :name, BIRTHDAY = :birthday WHERE ID = :id",
                Map.ofEntries(
                        entry("email", user.getEmail()),
                        entry("login", user.getLogin()),
                        entry("name", user.getName()),
                        entry("birthday", user.getBirthday()),
                        entry("id", user.getId())
                ));
        return user;
    }

    private User mapRowToUser(SqlRowSet userRow) {
        return User.builder()
                .id(userRow.getLong("ID"))
                .email(userRow.getString("EMAIL"))
                .login(userRow.getString("LOGIN"))
                .name(userRow.getString("NAME"))
                .birthday(userRow.getDate("BIRTHDAY").toLocalDate())
                .build();
    }
}
