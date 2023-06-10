package ru.yandex.practicum.filmorate.controller;

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
public class FilmController {
    private final FilmRepository repository = new FilmRepository();

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

    /*private void validateFilm(Film film) {
        if (film.getName().isEmpty()) {
            throw new InvalidValueException("Пустое название фильма");
        }
        if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidValueException("Слишком длинное описание фильма: " + film.getDescription().length()
                    + " символов");
        }
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new InvalidValueException("Дата релиза фильма ранее дня рождения кино: '" + film.getReleaseDate()
                    + "'");
        }
        if (film.getDuration() <= 0) {
            throw new InvalidValueException("Продолжительность фильма должна быть положительной: '"
                    + film.getDuration() + "'");
        }
    }*/
}
