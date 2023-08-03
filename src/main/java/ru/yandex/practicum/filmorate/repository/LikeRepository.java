package ru.yandex.practicum.filmorate.repository;

public interface LikeRepository {
    void addLike(Long filmId, Long userId, boolean isLike);

    void deleteLike(Long filmId, Long userId, boolean isLike);
}
