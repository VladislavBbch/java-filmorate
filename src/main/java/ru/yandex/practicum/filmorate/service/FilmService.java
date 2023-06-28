package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidValueException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final CrudRepository<Film> filmRepository;

    public List<Film> getFilms() {
        return filmRepository.read();
    }

    public Film getFilmById(Long filmId) {
        if (filmId <= 0) {
            throw new InvalidValueException("Некорректный id фильма: " + filmId);
        }
        return filmRepository.getById(filmId);
    }

    public Film createFilm(Film film) {
        return filmRepository.create(film);
    }

    public Film updateFilm(Film film) {
        Long id = film.getId();
        if (id <= 0) {
            throw new InvalidValueException("Некорректный id фильма: " + film.getId());
        }
        return filmRepository.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        if (filmId <= 0) {
            throw new InvalidValueException("Некорректный id фильма: " + userId);
        }
        if (userId <= 0) {
            throw new InvalidValueException("Некорректный id пользователя: " + userId);
        }
        Film film = filmRepository.getById(filmId);
        film.getLikedUsers().add(userId);
        film.setLikeCount(film.getLikeCount() + 1);
    }

    public void deleteLike(Long filmId, Long userId) {
        if (filmId <= 0) {
            throw new InvalidValueException("Некорректный id фильма: " + userId);
        }
        if (userId <= 0) {
            throw new InvalidValueException("Некорректный id пользователя: " + userId);
        }
        Film film = filmRepository.getById(filmId);
        film.getLikedUsers().remove(userId);
        film.setLikeCount(film.getLikeCount() - 1);
    }

    public List<Film> getMostPopularFilms(Integer count) {
        if (count == null || count <= 0) {
            throw new InvalidValueException("Некорректное значение параметра count");
        }
        List<Film> result = new ArrayList<>(filmRepository.read());
        result.sort((f1, f2) -> f2.getLikeCount() - f1.getLikeCount());
        if (result.size() < count) {
            count = result.size();
        }
        return result.subList(0, count);
    }
}
