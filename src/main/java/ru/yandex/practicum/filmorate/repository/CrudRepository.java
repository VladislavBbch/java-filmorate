package ru.yandex.practicum.filmorate.repository;

import java.util.List;

public interface CrudRepository<T> {
    T create(T t);

    List<T> read();

    T update(T t);

    T getById(Long id);

    void delete(Long id);
}
