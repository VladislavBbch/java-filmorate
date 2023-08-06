package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmRepository extends CrudRepository<Film> {
    List<Film> getMostPopularFilms(Integer count);

    List<Film> getCommonFilms(Long userId, Long friendId);

}
