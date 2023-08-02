package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmRepository extends CrudRepository<Film> {
    List<Film> getMostPopularFilms(Integer count);
    Set<Film> getDirectorFilms(long directorId, String sortBy);
}
