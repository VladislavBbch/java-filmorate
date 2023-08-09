package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @PostMapping
    public User createUser(@RequestBody @Valid User user) {
        log.info("Начало обработки запроса на создание пользователя: {}", user);
        User newUser = userService.createUser(user);
        log.info("Окончание обработки запроса на создание пользователя");
        return newUser;
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user) {
        log.info("Начало обработки запроса на обновление пользователя: {}", user);
        User existingUser = userService.updateUser(user);
        log.info("Окончание обработки запроса на обновление пользователя");
        return existingUser;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("Начало обработки запроса по получению пользователя: {}", id);
        User user = userService.getUserById(id);
        log.info("Окончание обработки запроса по получению пользователя");
        return user;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Начало обработки запроса на добавление в друзья: {} <-> {}", id, friendId);
        userService.addFriend(id, friendId);
        log.info("Окончание обработки запроса на добавление в друзья");
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Начало обработки запроса на удаление из друзей: {} <-> {}", id, friendId);
        userService.deleteFriend(id, friendId);
        log.info("Окончание обработки запроса на удаление из друзей");
    }

    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable Long id) {
        log.info("Начало обработки запроса по получению друзей пользователя: {}", id);
        List<User> result = userService.getUserFriends(id);
        log.info("Окончание обработки запроса по получению друзей пользователя");
        return result;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Начало обработки запроса по получению общих друзей для пользователей: {} <-> {}", id, otherId);
        List<User> result = userService.getCommonFriends(id, otherId);
        log.info("Окончание обработки запроса по получению общих друзей для пользователей");
        return result;
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") Long id) {
        log.info("Начало обработки запроса по удалению пользователя: {}", id);
        userService.deleteUser(id);
        log.info("Окончание обработки запроса по удалению пользователя");
    }

    @GetMapping("/{userId}/recommendations")
    public List<Film> getRecommendations(@PathVariable Long userId) {
        log.info("Начало обработки запроса по получению рекомендаций для пользователя с id: {}", userId);
        List<Film> result = userService.getRecommendations(userId);
        log.info("Окончание обработки запроса по получению рекомендаций для пользователя с id: {}", userId);
        return result;
    }

    @GetMapping("/{id}/feed")
    public List<Event> getUserFeed(@PathVariable Long id) {
        log.info("Начало обработки запроса по получению ленты событий пользователя: {}", id);
        List<Event> feed = userService.getUserFeed(id);
        log.info("Окончание обработки запроса по получению ленты событий пользователя");
        return feed;
    }
}
