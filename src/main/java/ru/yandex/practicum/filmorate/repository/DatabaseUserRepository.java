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

import java.util.*;

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
        return getById(user.getId());
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        //check already friends
        SqlRowSet resultRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT USER_ID, FRIEND_ID FROM FRIENDS WHERE USER_ID = :userId AND FRIEND_ID = :friendId AND IS_CONFIRMED = true " +
                        "UNION " +
                        "SELECT USER_ID, FRIEND_ID FROM FRIENDS WHERE USER_ID = :friendId AND FRIEND_ID = :userId AND IS_CONFIRMED = true",
                Map.ofEntries(
                        entry("userId", userId),
                        entry("friendId", friendId)
                ));
        if (resultRow.next()) {
            return;
        }
        //check unconfirmed friends
        resultRow = parameterJdbcTemplate.queryForRowSet("SELECT * FROM FRIENDS WHERE " +
                        "USER_ID = :userId AND FRIEND_ID = :friendId AND IS_CONFIRMED = false",
                Map.ofEntries(
                        entry("userId", friendId),
                        entry("friendId", userId)
                ));
        if (resultRow.next()) {
            parameterJdbcTemplate.update(
                    "UPDATE FRIENDS SET IS_CONFIRMED = true WHERE USER_ID = :userId AND FRIEND_ID = :friendId",
                    Map.ofEntries(
                            entry("userId", friendId),
                            entry("friendId", userId)
                    ));
            return;
        }
        //add unconfirmed friend
        addUnconfirmedFriend(userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        //if user added friend
        SqlRowSet resultRow = parameterJdbcTemplate.queryForRowSet("SELECT * FROM FRIENDS WHERE " +
                        "USER_ID = :userId AND FRIEND_ID = :friendId",
                Map.ofEntries(
                        entry("userId", userId),
                        entry("friendId", friendId)
                ));
        if (resultRow.next()) {
            //delete user -> friend
            parameterJdbcTemplate.update("DELETE FROM FRIENDS WHERE USER_ID = :userId AND FRIEND_ID = :friendId",
                    Map.ofEntries(
                            entry("userId", userId),
                            entry("friendId", friendId)
                    ));
            //add unconfirmed friend -> user
            if (resultRow.getBoolean("IS_CONFIRMED")) {
                addUnconfirmedFriend(friendId, userId);
            }
            return;
        }
        //if friend add user - set unconfirmed
        resultRow = parameterJdbcTemplate.queryForRowSet("SELECT * FROM FRIENDS WHERE " +
                        "USER_ID = :userId AND FRIEND_ID = :friendId",
                Map.ofEntries(
                        entry("userId", friendId),
                        entry("friendId", userId)
                ));
        if (resultRow.next()) {
            parameterJdbcTemplate.update(
                    "UPDATE FRIENDS SET IS_CONFIRMED = false WHERE USER_ID = :userId AND FRIEND_ID = :friendId",
                    Map.ofEntries(
                            entry("userId", friendId),
                            entry("friendId", userId)
                    ));
        }
    }

    @Override
    public List<User> getUserFriends(Long id) {
        List<User> friends = new ArrayList<>();
        SqlRowSet friendRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT FRIEND_ID AS ID, IS_CONFIRMED FROM FRIENDS WHERE USER_ID = :userId " +
                        "UNION " +
                        "SELECT USER_ID AS ID, IS_CONFIRMED FROM FRIENDS WHERE FRIEND_ID = :friendId AND IS_CONFIRMED = true",
                Map.ofEntries(
                        entry("userId", id),
                        entry("friendId", id)
                ));
        while (friendRow.next()) {
            friends.add(getById(friendRow.getLong("ID")));
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long friendId) {
        Set<User> firstUserFriends = new HashSet<>(getUserFriends(userId));
        Set<User> secondUserFriends = new HashSet<>(getUserFriends(friendId));
        firstUserFriends.retainAll(secondUserFriends);
        return new ArrayList<>(firstUserFriends);
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

    private void addUnconfirmedFriend(Long userId, Long friendId) {
        parameterJdbcTemplate.update(
                "INSERT INTO FRIENDS (USER_ID, FRIEND_ID, IS_CONFIRMED) VALUES (:userId, :friendId, :isConfirmed)",
                Map.ofEntries(
                        entry("userId", userId),
                        entry("friendId", friendId),
                        entry("isConfirmed", false)
                ));
    }
}
