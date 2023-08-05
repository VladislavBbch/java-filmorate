package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.*;

import java.util.ArrayList;
import java.util.Iterator;
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
    private final DirectorRepository directorRepository;

    public List<Film> getFilms() {
        List<Film> films = genreRepository.enrichFilmsByGenres(filmRepository.read());
        films.forEach(film -> film.setDirectors(directorRepository.getDirectorsByFilmId(film.getId())));
        return films;
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
        createdFilm.setDirectors(directorRepository.getDirectorsByFilmId(createdFilm.getId()));
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
        getFilmById(filmId); // Проверка на наличие фильма
        filmRepository.delete(filmId);
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

    public List<Film> getDirectorFilms(Long id, String sortBy) {
        List<Film> films = new ArrayList<>();
        for (Long aLong : directorRepository.getDirectorFilms(id, sortBy)) {
            films.add(filmRepository.getById(aLong));
        }
        films.forEach(film -> film.setDirectors(directorRepository.getDirectorsByFilmId(film.getId())));
        if (genreRepository.enrichFilmsByGenres(films) == null || films.isEmpty()) {
            throw new ObjectNotFoundException("Director Films Not Found");
        }
        return films;
    }
}
