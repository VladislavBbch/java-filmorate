package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) Long filmId,
                                   @RequestParam(required = false, defaultValue = "10") @Positive Integer count) {
        final String filmInfo = filmId == null ? " " : " для фильма с id:" + filmId;
        log.info("Начало получения отзывов в количестве {} штук{}", count, filmInfo);
        List<Review> reviews = reviewService.getReviews(filmId, count);
        log.info("Окончание получения отзывов в количестве {} штук{}", count, filmInfo);
        return reviews;
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        log.info("Начало обработки запроса на получение значения отзыва по id: {}", id);
        Review review = reviewService.getReviewById(id);
        log.info("Окончание обработки запроса на получение значения отзыва по id: {}", id);
        return review;
    }

    @PostMapping
    public Review createReview(@RequestBody @Valid Review review) {
        log.info("Начало обработки запроса на создание отзыва: {}", review);
        Review newReview = reviewService.createReview(review);
        log.info("Окончание обработки запроса на создание отзыва");
        return newReview;
    }

    @PutMapping
    public Review updateReview(@RequestBody @Valid Review review) {
        log.info("Начало обработки запроса на обновление отзыва: {}", review);
        Review existingReview = reviewService.updateReview(review);
        log.info("Окончание обработки запроса на обновление отзыва");
        return existingReview;
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        log.info("Начало обработки запроса на удаление отзыва по id: {}", id);
        reviewService.deleteReviewById(id);
        log.info("Окончание обработки запроса на удаление отзыва");
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLikeToReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Начало обработки поставить лайк отзыву по id: {} от пользователя с id: {}", id, userId);
        reviewService.putLikeToReview(id, userId, true);
        log.info("Окончание обработки поставить лайк отзыву по id: {} от пользователя с id: {}", id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void putDislikeToReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Начало обработки поставить дизлайк отзыву по id: {} от пользователя с id: {}", id, userId);
        reviewService.putLikeToReview(id, userId, false);
        log.info("Окончание обработки поставить дизлайк отзыву по id: {} от пользователя с id: {}", id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeToReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Начало обработки запроса на удаление лайка отзыву по id: {} от пользователя с id: {}", id, userId);
        reviewService.deleteLikeToReview(id, userId, true);
        log.info("Окончание обработки запроса на удаление лайка отзыву по id: {} от пользователя с id: {}", id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeToReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Начало обработки удалить дизлайк отзыву по id: {} от пользователя с id: {}", id, userId);
        reviewService.deleteLikeToReview(id, userId, false);
        log.info("Окончание обработки удалить дизлайк отзыву по id: {} от пользователя с id: {}", id, userId);
    }
}
