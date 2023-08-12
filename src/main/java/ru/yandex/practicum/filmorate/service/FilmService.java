package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidValueException;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.repository.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final RatingMpaRepository ratingMpaRepository;
    private final GenreRepository genreRepository;
    private final MarkRepository markRepository;
    private final DirectorRepository directorRepository;
    private final FeedRepository feedRepository;

    public List<Film> getFilms() {
        List<Film> films = filmRepository.read();
        directorRepository.enrichFilmDirectors(films);
        return genreRepository.enrichFilmsByGenres(films);
    }

    public Film getFilmById(Long filmId) {
        Film film = filmRepository.getById(filmId);
        if (film == null) {
            throw new ObjectNotFoundException("Несуществующий id фильма: " + filmId);
        }
        film.setGenres(genreRepository.getFilmGenres(filmId));
        film.setDirectors(directorRepository.getDirectorsByFilmId(filmId));
        return film;
    }

    public Film createFilm(Film film) {
        checkRatingMpa(film.getRatingMpa().getId());
        checkGenres(film.getGenres());
        Film createdFilm = filmRepository.create(film);
        genreRepository.addFilmGenres(createdFilm);
        createdFilm.setGenres(genreRepository.getFilmGenres(createdFilm.getId()));
        createdFilm.setDirectors(directorRepository.updateDirectors(film, createdFilm.getId(), true));
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        checkRatingMpa(film.getRatingMpa().getId());
        checkGenres(film.getGenres());
        Film updatedFilm = filmRepository.update(film);
        genreRepository.deleteFilmGenres(updatedFilm.getId());
        genreRepository.addFilmGenres(updatedFilm);
        updatedFilm.setGenres(genreRepository.getFilmGenres(updatedFilm.getId()));
        updatedFilm.setDirectors(directorRepository.updateDirectors(film, film.getId(), false));
        return updatedFilm;
    }

    public void deleteFilm(Long filmId) {
        getFilmById(filmId);
        filmRepository.delete(filmId);
    }

    public void addMark(Long filmId, Long userId, Integer mark) {
        getFilmById(filmId);
        checkUser(userId);
        markRepository.addMark(filmId, userId, mark);
        feedRepository.createEvent(userId, filmId, EventType.LIKE, Operation.ADD);
    }

    public void deleteMark(Long filmId, Long userId) {
        getFilmById(filmId);
        checkUser(userId);
        markRepository.deleteMark(filmId, userId);
        feedRepository.createEvent(userId, filmId, EventType.LIKE, Operation.REMOVE);
    }

    public List<Film> getMostPopularFilms(Integer count, Long genreId, Integer year) {
        if (genreId != null && genreRepository.getById(genreId) == null) {
            throw new ObjectNotFoundException("Несуществующий id жанра: " + genreId);
        }
        List<Film> popularFilms = filmRepository.getMostPopularFilms(count, genreId, year);
        directorRepository.enrichFilmDirectors(popularFilms);
        return genreRepository.enrichFilmsByGenres(popularFilms);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        checkUser(userId);
        checkUser(friendId);
        List<Film> commonFilms = filmRepository.getCommonFilms(userId, friendId);
        directorRepository.enrichFilmDirectors(commonFilms);
        return genreRepository.enrichFilmsByGenres(commonFilms);
    }

    public List<Film> getDirectorFilms(Long directorId, String sortBy) {
        if (!sortBy.equals("year") && !sortBy.equals("likes")) {
            throw new InvalidValueException("sortBy");
        }
        if (directorRepository.getById(directorId) == null) {
            throw new ObjectNotFoundException("Несуществующий id режиссера: " + directorId);
        }
        List<Film> films = directorRepository.getDirectorFilms(directorId, sortBy);
        if (films.isEmpty()) {
            throw new ObjectNotFoundException("Не найдены фильмы режиссера: " + directorId);
        }
        directorRepository.enrichFilmDirectors(films);
        return genreRepository.enrichFilmsByGenres(films);
    }

    public List<Film> searchFilms(String query, String by) {
        List<Film> searchedFilms;
        if (by.contains(",") && by.contains("director") && by.contains("title")) {
            searchedFilms = filmRepository.searchFilms(query, true, true);
        } else if (by.equals("director")) {
            searchedFilms = filmRepository.searchFilms(query, true, false);
        } else if (by.equals("title")) {
            searchedFilms = filmRepository.searchFilms(query, false, true);
        } else {
            throw new InvalidValueException("Некорректный признак поиска 'by': " + by);
        }
        directorRepository.enrichFilmDirectors(searchedFilms);
        return genreRepository.enrichFilmsByGenres(searchedFilms);
    }

    private void checkRatingMpa(Long id) {
        if (ratingMpaRepository.getById(id) == null) {
            throw new ObjectNotFoundException("Несуществующий id жанра: " + id);
        }
    }

    private void checkUser(Long id) {
        if (userRepository.getById(id) == null) {
            throw new ObjectNotFoundException("Несуществующий id пользователя: " + id);
        }
    }

    private void checkGenres(Set<Genre> genres) {
        if (genres == null || genres.size() == 0) {
            return;
        }
        List<Long> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toList());
        List<Genre> existingGenres = genreRepository.getByIds(genreIds);
        if (existingGenres == null || existingGenres.size() != genres.size()) {
            throw new ObjectNotFoundException("Найден несуществующий id жанра в списке: " + genreIds);
        }
    }
}
