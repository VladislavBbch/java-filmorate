package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
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
    private final LikeRepository likeRepository;

    public List<Film> getFilms() {
        return genreRepository.enrichFilmsByGenres(filmRepository.read());
    }

    public Film getFilmById(Long filmId) {
        Film film = filmRepository.getById(filmId);
        if (film == null) {
            throw new ObjectNotFoundException("Несуществующий id фильма: " + filmId);
        }
        film.setGenres(genreRepository.getFilmGenres(filmId));
        return film;
    }

    public Film createFilm(Film film) {
        checkRatingMpa(film.getRatingMpa().getId());
        checkGenres(film.getGenres());
        Film createdFilm = filmRepository.create(film);
        genreRepository.addFilmGenres(createdFilm);
        createdFilm.setGenres(genreRepository.getFilmGenres(createdFilm.getId()));
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
        return updatedFilm;
    }

    public void addLike(Long filmId, Long userId) {
        getFilmById(filmId);
        if (userRepository.getById(userId) == null) {
            throw new ObjectNotFoundException("Несуществующий id пользователя: " + userId);
        }
        likeRepository.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        getFilmById(filmId);
        if (userRepository.getById(userId) == null) {
            throw new ObjectNotFoundException("Несуществующий id пользователя: " + userId);
        }
        likeRepository.deleteLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(Integer count) {
        return genreRepository.enrichFilmsByGenres(filmRepository.getMostPopularFilms(count));
    }

    private void checkRatingMpa(Long id) {
        if (ratingMpaRepository.getById(id) == null) {
            throw new ObjectNotFoundException("Несуществующий id жанра: " + id);
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
