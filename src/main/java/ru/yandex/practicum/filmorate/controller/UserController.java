package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.InvalidValueException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository repository = new UserRepository();

    @GetMapping
    public List<User> getUsers() {
        return repository.read();
    }

    @PostMapping
    public User createUser(@RequestBody @Valid User user) {
        log.info("Начало обработки запроса на создание пользователя: " + user);
        User newUser = repository.create(user);
        log.info("Окончание обработки запроса на создание пользователя");
        return newUser;
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user) {
        log.info("Начало обработки запроса на обновление пользователя: " + user);
        int id = user.getId();
        if (id == 0) {
            throw new InvalidValueException("Некорректный id пользователя");
        }
        User existingUser = repository.update(user);
        log.info("Окончание обработки запроса на обновление пользователя");
        return existingUser;
    }

    /*private void validateUser(User user) {
        if (user.getEmail().isEmpty() || !user.getEmail().contains("@")) {
            throw new InvalidValueException("Некорректный адрес электронной почты: '" + user.getEmail() + "'");
        }
        if (user.getLogin().isEmpty() || user.getLogin().contains(" ")) {
            throw new InvalidValueException("Некорректный логин пользователя: '" + user.getLogin() + "'");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new InvalidValueException("День рождения пользователя не может быть в будущем: '" + user.getBirthday()
                    + "'");
        }
    }*/
}
