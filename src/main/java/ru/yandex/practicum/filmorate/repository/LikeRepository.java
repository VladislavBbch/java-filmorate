package ru.yandex.practicum.filmorate.repository;

public interface LikeRepository {
    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);
}
