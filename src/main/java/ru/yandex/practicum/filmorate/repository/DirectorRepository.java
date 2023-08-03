package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Director;


public interface DirectorRepository extends CrudRepository<Director> {
    void delete(Long id);
}
