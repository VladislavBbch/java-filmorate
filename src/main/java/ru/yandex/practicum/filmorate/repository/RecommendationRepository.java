package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface RecommendationRepository {
    List<Film> getByUserIdForLike(Long UserId);
}
