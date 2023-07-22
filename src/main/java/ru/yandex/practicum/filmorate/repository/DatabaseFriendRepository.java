package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

@Repository
@RequiredArgsConstructor
public class DatabaseFriendRepository implements FriendRepository {
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

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
                "SELECT F.FRIEND_ID AS ID, U.EMAIL, U.LOGIN, U.NAME, U.BIRTHDAY " +
                        "FROM FRIENDS AS F " +
                        "JOIN USERS AS U ON F.FRIEND_ID = U.ID " +
                        "WHERE USER_ID = :userId " +
                        "UNION " +
                        "SELECT F.USER_ID AS ID, U.EMAIL, U.LOGIN, U.NAME, U.BIRTHDAY " +
                        "FROM FRIENDS AS F " +
                        "JOIN USERS AS U ON F.USER_ID = U.ID " +
                        "WHERE FRIEND_ID = :friendId AND IS_CONFIRMED = true",
                Map.ofEntries(
                        entry("userId", id),
                        entry("friendId", id)
                ));
        while (friendRow.next()) {
            friends.add(mapRowToUser(friendRow));
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long friendId) {
        List<User> commonFriends = new ArrayList<>();
        SqlRowSet friendRow = parameterJdbcTemplate.queryForRowSet(
                "SELECT UF.ID, UF.EMAIL, UF.LOGIN, UF.NAME, UF.BIRTHDAY  FROM " +
                        "(SELECT F.FRIEND_ID AS ID, U.EMAIL, U.LOGIN, U.NAME, U.BIRTHDAY " +
                        "FROM FRIENDS AS F " +
                        "JOIN USERS AS U ON F.FRIEND_ID = U.ID " +
                        "WHERE USER_ID = :userId " +
                        "UNION " +
                        "SELECT F.USER_ID AS ID, U.EMAIL, U.LOGIN, U.NAME, U.BIRTHDAY " +
                        "FROM FRIENDS AS F " +
                        "JOIN USERS AS U ON F.USER_ID = U.ID " +
                        "WHERE FRIEND_ID = :userId AND IS_CONFIRMED = true) AS UF " +
                        "JOIN (SELECT F.FRIEND_ID AS ID " +
                        "FROM FRIENDS AS F " +
                        "WHERE USER_ID = :friendId " +
                        "UNION " +
                        "SELECT F.USER_ID AS ID " +
                        "FROM FRIENDS AS F " +
                        "WHERE FRIEND_ID = :friendId AND IS_CONFIRMED = true) AS FF " +
                        "ON UF.ID = FF.ID",
                Map.ofEntries(
                        entry("userId", userId),
                        entry("friendId", friendId)
                ));
        while (friendRow.next()) {
            commonFriends.add(mapRowToUser(friendRow));
        }
        return commonFriends;
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
