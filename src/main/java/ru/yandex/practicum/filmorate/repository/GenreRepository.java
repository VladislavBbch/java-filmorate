package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

public interface GenreRepository {
    List<Genre> read();

    Genre getById(Long id);

    List<Genre> getByIds(List<Long> ids);

    void addFilmGenres(Film film);

    void deleteFilmGenres(Long filmId);

    Set<Genre> getFilmGenres(Long filmId);

    List<Film> enrichFilmsByGenres(List<Film> films);
}
