package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmRepository extends CrudRepository<Film> {
    List<Film> getMostPopularFilms(Integer count, Integer year);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getRecommendationFilmByUserIdForLike(Long userId);

    List<Film> searchFilms(String query, Boolean byDirector, Boolean byTitle);
}
