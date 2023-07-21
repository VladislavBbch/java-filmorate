package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest
@Import(DatabaseUserRepository.class)
@DisplayName("Хранилище пользователей в базе данных должно:")
public class DatabaseUserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @DisplayName("возвращать по id")
    @Test
    public void shouldGetById() {
        User user = userRepository.getById(1L);
        assertNotNull(user);
        assertEquals(1L, user.getId(), "идентификатор");
        assertEquals("user1@test.ru", user.getEmail(), "эл. почта");
        assertEquals("Пользователь1", user.getLogin(), "логин");
        assertEquals("Имя Фамилия1", user.getName(), "имя");
        assertEquals(LocalDate.parse("1991-05-05"), user.getBirthday(), "дата рождения");
    }

    @DisplayName("создавать")
    @Test
    public void shouldCreate() {
        User user = userRepository.create(User.builder()
                .email("user7@test.ru")
                .login("Пользователь7")
                .name("Имя Фамилия7")
                .birthday(LocalDate.parse("1997-05-05"))
                .build());
        assertNotNull(user);
        assertEquals(7L, user.getId(), "идентификатор");
        assertEquals("user7@test.ru", user.getEmail(), "эл. почта");
        assertEquals("Пользователь7", user.getLogin(), "логин");
        assertEquals("Имя Фамилия7", user.getName(), "имя");
        assertEquals(LocalDate.parse("1997-05-05"), user.getBirthday(), "дата рождения");
    }

    @DisplayName("возвращать всех")
    @Test
    public void shouldGetAll() {
        List<User> users = userRepository.read();
        assertNotNull(users);
        assertEquals(6, users.size());
        User user = users.get(0);
        assertNotNull(user);
        assertEquals(1L, user.getId(), "идентификатор1");
        user = users.get(1);
        assertNotNull(user);
        assertEquals(2L, user.getId(), "идентификатор2");
        user = users.get(2);
        assertNotNull(user);
        assertEquals(3L, user.getId(), "идентификатор3");
        user = users.get(3);
        assertNotNull(user);
        assertEquals(4L, user.getId(), "идентификатор4");
        user = users.get(4);
        assertNotNull(user);
        assertEquals(5L, user.getId(), "идентификатор5");
        user = users.get(5);
        assertNotNull(user);
        assertEquals(6L, user.getId(), "идентификатор5");
    }

    @DisplayName("обновлять")
    @Test
    public void shouldUpdate() {
        User user = userRepository.update(User.builder()
                .id(1L)
                .email("updatedUser@test.ru")
                .login("ОбновленныйПользователь")
                .name("Новые Имя Фамилия")
                .birthday(LocalDate.parse("1990-05-05"))
                .build());
        assertNotNull(user);
        assertEquals(1L, user.getId(), "идентификатор");
        assertEquals("updatedUser@test.ru", user.getEmail(), "эл. почта");
        assertEquals("ОбновленныйПользователь", user.getLogin(), "логин");
        assertEquals("Новые Имя Фамилия", user.getName(), "имя");
        assertEquals(LocalDate.parse("1990-05-05"), user.getBirthday(), "дата рождения");
    }

    @DisplayName("возвращать друзей пользователя")
    @Test
    public void shouldGetUserFriends() {
        List<User> friends = userRepository.getUserFriends(3L);
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
        List<User> friends = userRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(3, friends.size(), "пользователь 3, количество друзей 3");
        friends = userRepository.getUserFriends(2L);
        assertNotNull(friends);
        assertEquals(1, friends.size(), "пользователь 2, количество друзей 1");

        userRepository.addFriend(3L, 2L); //confirm

        friends = userRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(4, friends.size(), "пользователь 3, количество друзей 4");
        friends = userRepository.getUserFriends(2L);
        assertNotNull(friends);
        assertEquals(1, friends.size(), "пользователь 2, количество друзей 1");

        friends = userRepository.getUserFriends(6L);
        assertNotNull(friends);
        assertEquals(0, friends.size(), "пользователь 6, количество друзей 0");

        userRepository.addFriend(6L, 3L); //add without confirm

        friends = userRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(4, friends.size(), "пользователь 3, количество друзей 4");
        friends = userRepository.getUserFriends(6L);
        assertNotNull(friends);
        assertEquals(1, friends.size(), "пользователь 6, количество друзей 1");
    }

    @DisplayName("удалять из друзей")
    @Test
    public void shouldDeleteFriend() {
        List<User> friends = userRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(3, friends.size(), "пользователь 3, количество друзей 3");
        friends = userRepository.getUserFriends(1L);
        assertNotNull(friends);
        assertEquals(1, friends.size(), "пользователь 1, количество друзей 1");

        userRepository.deleteFriend(3L, 1L); //unconfirm

        friends = userRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(2, friends.size(), "пользователь 3, количество друзей 2");
        friends = userRepository.getUserFriends(1L);
        assertNotNull(friends);
        assertEquals(1, friends.size(), "пользователь 1, количество друзей 1");

        friends = userRepository.getUserFriends(5L);
        assertNotNull(friends);
        assertEquals(0, friends.size(), "пользователь 5, количество друзей 0");

        userRepository.deleteFriend(3L, 5L); //delete unconfirmed

        friends = userRepository.getUserFriends(3L);
        assertNotNull(friends);
        assertEquals(1, friends.size(), "пользователь 3, количество друзей 1");
        friends = userRepository.getUserFriends(5L);
        assertNotNull(friends);
        assertEquals(0, friends.size(), "пользователь 5, количество друзей 0");
    }

    @DisplayName("возвращать общих друзей")
    @Test
    public void shouldGetCommonFriends() {
        List<User> commonFriends = userRepository.getCommonFriends(1L, 4L);
        assertNotNull(commonFriends);
        assertEquals(1, commonFriends.size());
        User friend = commonFriends.get(0);
        assertEquals(3L, friend.getId(), "идентификатор3");
    }

}
