package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.exception.InvalidValueException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilmRepository {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextFilmId = 1;

    public Film create(Film film) {
        Film newFilm = film
                .toBuilder()
                .id(nextFilmId++)
                .build();
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    public List<Film> read() {
        return List.copyOf(films.values());
    }

    public Film update(Film film) {
        Film existingFilm = films.get(film.getId());
        if (existingFilm == null) {
            throw new InvalidValueException("Несуществующий id фильма");
        }

        existingFilm.setName(film.getName());
        existingFilm.setDescription(film.getDescription());
        existingFilm.setDuration(film.getDuration());
        existingFilm.setReleaseDate(film.getReleaseDate());
        return existingFilm;
    }
}
