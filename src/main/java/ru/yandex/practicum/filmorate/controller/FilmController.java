package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
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
        log.info("Начало обработки запроса на получение списка всех фильмов");
        List<Film> films = filmService.getFilms();
        log.info("Окончание обработки запроса на получение списка всех фильмов");
        return films;
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
    public void addMark(@PathVariable Long id,
                        @PathVariable Long userId,
                        @RequestParam(defaultValue = "10") @Min(1) @Max(10) Integer mark) {
        log.info("Начало обработки запроса на добавление оценки '{}' фильму: \"{}\" от пользователя: {}", mark, id, userId);
        filmService.addMark(id, userId, mark);
        log.info("Окончание обработки запроса на добавление оценки фильму от пользователя");
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteMark(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Начало обработки запроса на удаление оценки фильму: {} от пользователя: {}", id, userId);
        filmService.deleteMark(id, userId);
        log.info("Окончание обработки запроса на удаление оценки фильму от пользователя");
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@RequestParam(defaultValue = "10") @Positive Integer count,
                                          @RequestParam(required = false) Long genreId,
                                          @RequestParam(required = false) @Min(1895) Integer year) { //см FilmReleaseDateValidator
        log.info("Начало обработки запроса на получение списка популярных фильмов");
        List<Film> mostPopularFilms = filmService.getMostPopularFilms(count, genreId, year);
        log.info("Окончание обработки запроса на получение списка популярных фильмов");
        return mostPopularFilms;
    }

    @GetMapping("director/{directorId}")
    public List<Film> getDirectorFilms(@PathVariable Long directorId, @RequestParam String sortBy) {
        log.info("Начало обработки запроса на получение фильмов режиссера {}", directorId);
        List<Film> directorFilms = filmService.getDirectorFilms(directorId, sortBy);
        log.info("Окончание обработки запроса на получение фильмов режиссера {}", directorId);
        return directorFilms;
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam Long friendId, @RequestParam Long userId) {
        log.info("Начало обработки запроса на получение общих фильмов пользователей {} и {}", userId, friendId);
        List<Film> commonFilms = filmService.getCommonFilms(userId, friendId);
        log.info("Окончание обработки запроса на получение общих фильмов");
        return commonFilms;
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable Long id) {
        log.info("Начало обработки запроса по удалению фильма: {}", id);
        filmService.deleteFilm(id);
        log.info("Окончание обработки запроса по удалению фильма");
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam @NotBlank String query,
                                  @RequestParam @NotBlank String by) {
        log.info("Начало обработки запроса по поиску фильмов по тексту: {} с признаком: {}", query, by);
        List<Film> films = filmService.searchFilms(query, by);
        log.info("Окончание обработки запроса по поиску фильмов по тексту: {} с признаком: {}", query, by);
        return films;
    }
}
