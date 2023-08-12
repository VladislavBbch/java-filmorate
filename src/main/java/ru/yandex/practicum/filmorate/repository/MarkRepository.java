package ru.yandex.practicum.filmorate.repository;

public interface MarkRepository {
    void addMark(Long filmId, Long userId, Integer mark);

    void deleteMark(Long filmId, Long userId);
}
