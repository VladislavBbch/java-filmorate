package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.DatabaseReviewLikeRepository;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.ReviewRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final DatabaseReviewLikeRepository likeRepository;

    public List<Review> getReviews(Long filmId, Integer count) {
        if (filmId == null) {
            return reviewRepository.getAllReviews(count);
        } else {
            return reviewRepository.getAllReviewsForFilmById(filmId, count);
        }
    }

    public Review getReviewById(Long id) {
       return findReviewById(id);
    }

    public Review createReview(Review review) {
        checkReview(review);
        review.setUsefulness(0);
        return reviewRepository.create(review);
    }

    public Review updateReview(Review review) {
        final Review findReview = findReviewById(review.getReviewId());
        review.setUserId(findReview.getUserId());
        review.setFilmId(findReview.getFilmId());
        review.setUsefulness(findReview.getUsefulness());
        return reviewRepository.update(review);
    }

    public void deleteReviewById(Long id) {
        reviewRepository.delete(id);
    }

    public void putLikeToReview(Long id, Long userId, boolean isLike) {
        final Review review = findReviewById(id);
        likeRepository.addLike(id, userId, isLike);
        controlUsefulForReview(review);
        reviewRepository.update(review);
    }

    public void deleteLikeToReview(Long id, Long userId, boolean isLike) {
        final Review review = findReviewById(id);
        likeRepository.deleteLike(id, userId, isLike);
        controlUsefulForReview(review);
        reviewRepository.update(review);
    }

    private Review findReviewById(Long id) {
        final Review review = reviewRepository.getById(id);
        if (review == null) {
            throw new ObjectNotFoundException("Несуществующий id отзыва: " + id);
        }
        return review;
    }

    private void checkReview(Review review) {
        Film film = filmRepository.getById(review.getFilmId());
        if (film == null) {
            throw new ObjectNotFoundException("Несуществующий id фильма: " + review.getFilmId());
        }

        User user = userRepository.getById(review.getUserId());
        if (user == null) {
            throw new ObjectNotFoundException("Несуществующий id пользователя: " + review.getUserId());
        }
    }

    private void controlUsefulForReview(Review review) {
        review.setUsefulness(reviewRepository.getUsefulnessReviewById(review.getReviewId()));
    }
}
