package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewRepository {

    List<Review> getAllReviews(Integer count);

    List<Review> getAllReviewsForFilmById(Long filmId, Integer count);

    int getUsefulnessReviewById(Long id);

    Review create(Review review);

    Review update(Review review);

    void delete(Long id);

    Review getById(Long id);
}
