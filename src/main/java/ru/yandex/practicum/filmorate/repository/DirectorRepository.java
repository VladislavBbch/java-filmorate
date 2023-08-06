package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;


public interface DirectorRepository extends CrudRepository<Director> {

    Set<Director> updateDirectors(Film film, long id, boolean isNew);

    List<Film> getDirectorFilms(Long directorId, String sortBy);

    Set<Director> getDirectorsByFilmId(long filmId);

    void enrichFilmDirectors(List<Film> films);
}
