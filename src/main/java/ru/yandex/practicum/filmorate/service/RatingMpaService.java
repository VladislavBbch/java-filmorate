package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.repository.RatingMpaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingMpaService {
    private final RatingMpaRepository mpaRepository;

    public List<RatingMpa> getAllRatingMpa() {
        return mpaRepository.read();
    }

    public RatingMpa getRatingMpaById(Long ratingMpaId) {
        RatingMpa ratingMpa = mpaRepository.getById(ratingMpaId);
        if (ratingMpa == null) {
            throw new ObjectNotFoundException("Несуществующий id рейтинга MPA: " + ratingMpaId);
        }
        return ratingMpa;
    }

}