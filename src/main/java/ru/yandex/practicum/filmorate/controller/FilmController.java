package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.InvalidValueException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Validated
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @PostMapping
    public Film createFilm(@RequestBody @Valid Film film) {
        log.info("Начало обработки запроса на создание фильма: {}", film);
        Film newFilm = filmService.createFilm(film);
        log.info("Окончание обработки запроса на создание фильма");
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film) {
        log.info("Начало обработки запроса на обновление фильма: {}", film);
        Long id = film.getId();
        if (id <= 0) {
            throw new InvalidValueException("Некорректный id фильма");
        }
        Film existingFilm = filmService.updateFilm(film);
        log.info("Окончание обработки запроса на обновление фильма");
        return existingFilm;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Начало обработки запроса по получению фильма: {}", id);
        Film film = filmService.getFilmById(id);
        log.info("Окончание обработки запроса по получению фильма");
        return film;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Начало обработки запроса на добавление лайка фильму: {} от пользователя: {}", id, userId);
        filmService.addLike(id, userId);
        log.info("Окончание обработки запроса на добавление лайка фильму от пользователя");
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Начало обработки запроса на удаление лайка фильму: {} от пользователя: {}", id, userId);
        filmService.deleteLike(id, userId);
        log.info("Окончание обработки запроса на удаление лайка фильму от пользователя");
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@RequestParam(defaultValue = "10") @Positive Integer count) {
        return filmService.getMostPopularFilms(count);
    }
}
