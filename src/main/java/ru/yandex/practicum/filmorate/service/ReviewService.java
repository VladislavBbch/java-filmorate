package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final FeedRepository feedRepository;

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
        final Review createdReview = reviewRepository.create(review);
        feedRepository.createEvent(createdReview.getUserId(), createdReview.getReviewId(),
                EventType.REVIEW, Operation.ADD);
        return createdReview;
    }

    public Review updateReview(Review review) {
        final Review findReview = findReviewById(review.getReviewId());
        findReview.setContent(review.getContent());
        findReview.setIsPositive(review.getIsPositive());
        feedRepository.createEvent(findReview.getUserId(), findReview.getReviewId(), EventType.REVIEW, Operation.UPDATE);
        return reviewRepository.update(findReview);
    }

    public void deleteReviewById(Long id) {
        final Review review = reviewRepository.getById(id);
        feedRepository.createEvent(review.getUserId(), review.getReviewId(), EventType.REVIEW, Operation.REMOVE);
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
