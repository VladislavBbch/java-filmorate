package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FeedRepository feedRepository;
    private final GenreRepository genreRepository;
    private final FilmRepository filmRepository;
    private final DirectorRepository directorRepository;

    public User createUser(User user) {
        checkUserName(user);
        return userRepository.create(user);
    }

    public List<User> getUsers() {
        return userRepository.read();
    }

    public User getUserById(Long userId) {
        User user = userRepository.getById(userId);
        if (user == null) {
            throw new ObjectNotFoundException("Несуществующий id пользователя: " + userId);
        }
        return user;
    }

    public User updateUser(User user) {
        checkUserName(user);
        getUserById(user.getId());
        return userRepository.update(user);
    }

    public void deleteUser(Long userId) {
        getUserById(userId); // Проверка на наличие пользователя
        userRepository.delete(userId);
    }

    public void addFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);
        friendRepository.addFriend(userId, friendId);
        feedRepository.createEvent(userId, friendId, EventType.FRIEND, Operation.ADD);
    }

    public void deleteFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);
        friendRepository.deleteFriend(userId, friendId);
        feedRepository.createEvent(userId, friendId, EventType.FRIEND, Operation.REMOVE);
    }

    public List<User> getUserFriends(Long userId) {
        getUserById(userId);
        return friendRepository.getUserFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);
        return friendRepository.getCommonFriends(userId, friendId);
    }

    public List<Event> getUserFeed(Long id) {
        getUserById(id);
        return feedRepository.getUserFeed(id);
    }

    public List<Film> getRecommendationsByMarkCount(Long userId) {
        getUserById(userId);
        List<Film> films = genreRepository.enrichFilmsByGenres(filmRepository.getRecommendationFilmByUserIdForLike(userId));
        directorRepository.enrichFilmDirectors(films);
        return films;
    }

    public List<Film> getRecommendations(Long userId) {
        getUserById(userId);
        //  userId,  filmId, mark
        Map<Long, Map<Long, Long>> marks = filmRepository.getFilmsMarksByUsers();
        Map<Long, Map<Long, Double>> diff = new HashMap<>();
        Map<Long, Map<Long, Integer>> freq = new HashMap<>();
        List<Film> allFilms = filmRepository.read();
        buildDifferencesMatrix(marks, diff, freq);
        Map<Long, Map<Long, Double>> predictedMarks = predict(marks, diff, freq,
                allFilms.stream().map(Film::getId).collect(Collectors.toList()));
        Map<Long, Double> predictedForUser = predictedMarks.get(userId);
        if (predictedForUser == null) {
            return new ArrayList<>();
        }
        Map<Long, Double> filteredForUser = predictedForUser.entrySet().stream()
                .filter(entry -> entry.getValue() > 5)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        List<Film> films = allFilms.stream()
                .filter(film -> filteredForUser.containsKey(film.getId()))
                .collect(Collectors.toList());
        directorRepository.enrichFilmDirectors(films);
        return genreRepository.enrichFilmsByGenres(films);
    }

    private void checkUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void buildDifferencesMatrix(
            Map<Long, Map<Long, Long>> data,
            Map<Long, Map<Long, Double>> diff,
            Map<Long, Map<Long, Integer>> freq) {
        for (Map<Long, Long> user : data.values()) { // проходим по всем пользователям
            for (Map.Entry<Long, Long> e : user.entrySet()) { // для каждого пользователя проходим по всем предметам, которые он оценивал
                if (!diff.containsKey(e.getKey())) {
                    diff.put(e.getKey(), new HashMap<>());
                    freq.put(e.getKey(), new HashMap<>());
                }
                for (Map.Entry<Long, Long> e2 : user.entrySet()) { // вложенный цикл, в нем тоже проходим по всем предметам, которые оценивал пользователь.
                    // Таким образом пройдемся по всем парам предметов, которые оценил пользователь. Например, есть оценки для предметов i1, i2 и i3.
                    // Благодаря этим двум циклам просмотрим пары (i1, i1), (i1, i2), (i1, i3), (i2, i1), (i2, i2), (i2, i3), (i3, i1), (i3, i2), (i3, i3)
                    int oldCount = 0;
                    if (freq.get(e.getKey()).containsKey(e2.getKey())) {
                        oldCount = freq.get(e.getKey()).get(e2.getKey());
                    }
                    double oldDiff = 0.0;
                    if (diff.get(e.getKey()).containsKey(e2.getKey())) {
                        oldDiff = diff.get(e.getKey()).get(e2.getKey());
                    }
                    double observedDiff = e.getValue() - e2.getValue(); // считаем разницу в оценках для текущей пары предметов
                    freq.get(e.getKey()).put(e2.getKey(), oldCount + 1); // увеличиваем счетчик на 1. Счетчик показывает, сколько раз мы просмотрели текущую пару предметов.
                    // Например, два разных пользователя оценили предметы i2 и i3. Тогда счетчик будет равен 2.
                    diff.get(e.getKey()).put(e2.getKey(), oldDiff + observedDiff); // Прибавляем полученную разницу в оценках к тому, что уже было в мапе (там уже была
                    // сумма по разницам в оценках от других пользователей)
                }
            }
        }
        for (Long j : diff.keySet()) { // двойным циклом проходим по всем просмотренным парам предметов
            for (Long i : diff.get(j).keySet()) {
                double oldValue = diff.get(j).get(i);
                int count = freq.get(j).get(i);
                diff.get(j).put(i, oldValue / count); // для каждой пары предметов считаем СРЕДНУЮЮ РАЗНИЦУ в оценках. Например, два пользователя оценили два предметак так:
                // 0.6 и 0.8 от первого пользователя и 0.7 и 0.3 от второго. Тогда средняя разница равна: ((0.6 - 0.8) + (0.7 - 0.3)) / 2 =  (-0.2 + 0.4) / 2 = 0.1
            }
        }
    }

    private Map<Long, Map<Long, Double>> predict(
            Map<Long, Map<Long, Long>> data,
            Map<Long, Map<Long, Double>> diff,
            Map<Long, Map<Long, Integer>> freq,
            List<Long> filmIds) {
        Map<Long, Map<Long, Double>> result = new HashMap<>();
        HashMap<Long, Double> uPred = new HashMap<>();
        HashMap<Long, Integer> uFreq = new HashMap<>();
        for (Long j : diff.keySet()) {
            uFreq.put(j, 0);
            uPred.put(j, 0.0);
        }
        for (Map.Entry<Long, Map<Long, Long>> e : data.entrySet()) { // проходим по всем пользователям
            for (Long j : e.getValue().keySet()) { // проходим по всем предметам, которые пользователь уже оценил
                for (Long k : diff.keySet()) { // проходим по всем предметам, для которых у нас есть информация о схожести с другими предметами
                    try {
                        double predictedValue = diff.get(k).get(j) + e.getValue().get(j).doubleValue(); // для пары предметов k,j (где j -
                        // это оцененный пользователем предмет, а k - предмет, оценку которого предсказываем) к их похожести прибавляем оценку предмета j пользователем.
                        // Если предметы очень похожи, то оценка пользователя почти не изменится. Если предметы сильно не похожи, то оценка пользователя сильно изменится
                        double finalValue = predictedValue * freq.get(k).get(j); // Полученную предсказанную оценку умножаем на количество пользователей, чьи оценки
                        // участвовали в вычислении похожести этой пары. Чем больше пользователей, тем точнее наша предсказанная оценка
                        uPred.put(k, uPred.get(k) + finalValue); // кладем полученное значение в мапу uPred
                        uFreq.put(k, uFreq.get(k) + freq.get(k).get(j)); // кладем количество пользователей, участвовавших в вычислении схожести, в мапу uFreq
                    } catch (NullPointerException ignored) {
                    }
                }
            }
            Map<Long, Double> clean = new HashMap<>();
            for (Long j : uPred.keySet()) {
                if (uFreq.get(j) > 0) {
                    clean.put(j, uPred.get(j) / uFreq.get(j)); // вычисляем среднее, это и есть предсказанная оценка
                }
            }
            for (Long j : filmIds) {
                if (e.getValue().containsKey(j)) {
                    clean.put(j, -1.0); // если пользователь уже оценивал фильм j, то забываем про предсказание и используем ту оценку, которую он поставил
// VladislavBbch: поставил -1 вместо оценки пользователя Double.valueOf(e.getValue().get(j)), чтобы потом не разбираться какие он уже смотрел
                } else if (!clean.containsKey(j)) {
                    clean.put(j, -1.0); // если предсказать не удалось, ставим -1.0
                }
            }
            result.put(e.getKey(), clean);
            // ВНИМАНИЕ! В алгоритме, кажется, ошибка. На мой взгляд, мапы uPred и uFreq нужно очищать перед каждым пользователем.
        }
        return result;
    }
}
