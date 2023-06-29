package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidValueException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.CrudRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final CrudRepository<Film> filmRepository;

    public List<Film> getFilms() {
        return filmRepository.read();
    }

    public Film getFilmById(Long filmId) {
        checkId(filmId);
        return filmRepository.getById(filmId);
    }

    public Film createFilm(Film film) {
        return filmRepository.create(film);
    }

    public Film updateFilm(Film film) {
        checkId(film.getId());
        return filmRepository.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        checkId(filmId);
        checkId(userId);
        Film film = filmRepository.getById(filmId);
        film.getLikedUsers().add(userId);
        film.setLikeCount(film.getLikeCount() + 1);
    }

    public void deleteLike(Long filmId, Long userId) {
        checkId(filmId);
        checkId(userId);
        Film film = filmRepository.getById(filmId);
        film.getLikedUsers().remove(userId);
        film.setLikeCount(film.getLikeCount() - 1);
    }

    public List<Film> getMostPopularFilms(Integer count) {
        return filmRepository.read().stream()
                .sorted((f1, f2) -> f2.getLikeCount() - f1.getLikeCount())
                .limit(count)
                .collect(Collectors.toList());
    }

    private void checkId(Long id) {
        if (id <= 0) {
            throw new InvalidValueException("Некорректный id: " + id);
        }
    }
}
