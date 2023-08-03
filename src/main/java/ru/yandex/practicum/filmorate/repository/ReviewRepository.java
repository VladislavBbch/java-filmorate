package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewRepository extends CrudRepository<Review> {

    List<Review> getAllReviews(Integer count);

    List<Review> getAllReviewsForFilmById(Long filmId, Integer count);

    int getUsefulnessReviewById(Long id);

    void delete(Long id);
}
