package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmRepository extends CrudRepository<Film> {
    List<Film> getMostPopularFilms(Integer count, Long genreId, Integer year);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getRecommendationFilmByUserIdForLike(Long userId);

    List<Film> searchFilms(String query, Boolean byDirector, Boolean byTitle);

    Map<Long, Map<Long, Long>> getFilmsMarksByUsers();
}
