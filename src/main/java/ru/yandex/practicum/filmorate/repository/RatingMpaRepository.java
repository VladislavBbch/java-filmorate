package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.util.List;

public interface RatingMpaRepository {
    List<RatingMpa> read();

    RatingMpa getById(Long id);
}
