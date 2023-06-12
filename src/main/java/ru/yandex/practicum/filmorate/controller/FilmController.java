package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.InvalidValueException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmRepository repository;

    @GetMapping
    public List<Film> getFilms() {
        return repository.read();
    }

    @PostMapping
    public Film createFilm(@RequestBody @Valid Film film) {
        log.info("Начало обработки запроса на создание фильма: " + film);
        Film newFilm = repository.create(film);
        log.info("Окончание обработки запроса на создание фильма");
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film) {
        log.info("Начало обработки запроса на обновление фильма: " + film);
        int id = film.getId();
        if (id == 0) {
            throw new InvalidValueException("Некорректный id фильма");
        }
        Film existingFilm = repository.update(film);
        log.info("Окончание обработки запроса на обновление фильма");
        return existingFilm;
    }
}
