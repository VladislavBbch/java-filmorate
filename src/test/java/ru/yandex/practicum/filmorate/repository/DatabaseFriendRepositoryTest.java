package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest
@Import(DatabaseFriendRepository.class)
@DisplayName("Хранилище друзей в базе данных должно:")
public class DatabaseFriendRepositoryTest {
    @Autowired
    private FriendRepository friendRepository;

    @DisplayName("возвращать друзей пользователя")
    @Test
    public void shouldGetUserFriends() {
        List<User> friends = friendRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(3, friends.size());
        List<Long> friendIds = friends.stream()
                .map(User::getId)
                .sorted()
                .collect(Collectors.toList());

        User friend = friends.get(0);
        assertNotNull(friend);
        assertEquals(1L, friendIds.get(0), "идентификатор1");
        friend = friends.get(1);
        assertNotNull(friend);
        assertEquals(4L, friendIds.get(1), "идентификатор4");
        friend = friends.get(2);
        assertNotNull(friend);
        assertEquals(5L, friendIds.get(2), "идентификатор5");
    }

    @DisplayName("добавлять в друзья")
    @Test
    public void shouldAddFriend() {
        List<User> friends = friendRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(3, friends.size(), "пользователь 3, количество друзей 3");
        friends = friendRepository.getUserFriends(2L);
        assertNotNull(friends);
        assertEquals(1, friends.size(), "пользователь 2, количество друзей 1");

        friendRepository.addFriend(3L, 2L); //confirm

        friends = friendRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(4, friends.size(), "пользователь 3, количество друзей 4");
        friends = friendRepository.getUserFriends(2L);
        assertNotNull(friends);
        assertEquals(1, friends.size(), "пользователь 2, количество друзей 1");

        friends = friendRepository.getUserFriends(6L);
        assertNotNull(friends);
        assertEquals(0, friends.size(), "пользователь 6, количество друзей 0");

        friendRepository.addFriend(6L, 3L); //add without confirm

        friends = friendRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(4, friends.size(), "пользователь 3, количество друзей 4");
        friends = friendRepository.getUserFriends(6L);
        assertNotNull(friends);
        assertEquals(1, friends.size(), "пользователь 6, количество друзей 1");
    }

    @DisplayName("удалять из друзей")
    @Test
    public void shouldDeleteFriend() {
        List<User> friends = friendRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(3, friends.size(), "пользователь 3, количество друзей 3");
        friends = friendRepository.getUserFriends(1L);
        assertNotNull(friends);
        assertEquals(1, friends.size(), "пользователь 1, количество друзей 1");

        friendRepository.deleteFriend(3L, 1L); //unconfirm

        friends = friendRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(2, friends.size(), "пользователь 3, количество друзей 2");
        friends = friendRepository.getUserFriends(1L);
        assertNotNull(friends);
        assertEquals(1, friends.size(), "пользователь 1, количество друзей 1");

        friends = friendRepository.getUserFriends(5L);
        assertNotNull(friends);
        assertEquals(0, friends.size(), "пользователь 5, количество друзей 0");

        friendRepository.deleteFriend(3L, 5L); //delete unconfirmed

        friends = friendRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(1, friends.size(), "пользователь 3, количество друзей 1");
        friends = friendRepository.getUserFriends(5L);
        assertNotNull(friends);
        assertEquals(0, friends.size(), "пользователь 5, количество друзей 0");
    }

    @DisplayName("возвращать общих друзей")
    @Test
    public void shouldGetCommonFriends() {
        List<User> commonFriends = friendRepository.getCommonFriends(1L, 4L);
        assertNotNull(commonFriends);
        assertEquals(1, commonFriends.size());
        User friend = commonFriends.get(0);
        assertEquals(3L, friend.getId(), "идентификатор3");
    }
}