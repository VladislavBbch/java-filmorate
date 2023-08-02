package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewRepository extends CrudRepository<Review> {
    Review deleteReview(Review review);

    List<Review> getAllReviews(Integer count);

    List<Review> getAllReviewsForFilmById(Long filmId, Integer count);

    void deleteUsefulReviewById(Long id);

    int getUsefulReviewById(Long id);

    void addLike(Long id, Long userId, boolean isLike);

    void deleteLike(Long id, Long userId, boolean isLike);
}
