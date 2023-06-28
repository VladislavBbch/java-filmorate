package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidValueException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.CrudRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final CrudRepository<User> userRepository;

    public User createUser(User user) {
        checkUserName(user);
        return userRepository.create(user);
    }

    public List<User> getUsers() {
        return userRepository.read();
    }

    public User getUserById(Long userId) {
        if (userId <= 0) {
            throw new InvalidValueException("Некорректный id пользователя: " + userId);
        }
        return userRepository.getById(userId);
    }

    public User updateUser(User user) {
        if (user.getId() <= 0) {
            throw new InvalidValueException("Некорректный id пользователя: " + user.getId());
        }
        checkUserName(user);
        return userRepository.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId <= 0) {
            throw new InvalidValueException("Некорректный id пользователя: " + userId);
        }
        if (friendId <= 0) {
            throw new InvalidValueException("Некорректный id пользователя: " + userId);
        }
        userRepository.getById(userId).getFriends().add(friendId);
        userRepository.getById(friendId).getFriends().add(userId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        if (userId <= 0) {
            throw new InvalidValueException("Некорректный id пользователя: " + userId);
        }
        if (friendId <= 0) {
            throw new InvalidValueException("Некорректный id пользователя: " + userId);
        }
        userRepository.getById(userId).getFriends().remove(friendId);
        userRepository.getById(friendId).getFriends().remove(userId);
    }

    public List<User> getUserFriends(Long userId) { //list<user>
        if (userId <= 0) {
            throw new InvalidValueException("Некорректный id пользователя: " + userId);
        }
        List<User> result = new ArrayList<>();
        for (Long id : userRepository.getById(userId).getFriends()) {
            result.add(userRepository.getById(id));
        }
        return result;
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        if (userId <= 0) {
            throw new InvalidValueException("Некорректный id пользователя: " + userId);
        }
        if (friendId <= 0) {
            throw new InvalidValueException("Некорректный id пользователя: " + userId);
        }
        Set<Long> firstUserFriends = new HashSet<>(userRepository.getById(userId).getFriends());
        Set<Long> secondUserFriends = userRepository.getById(friendId).getFriends();
        firstUserFriends.retainAll(secondUserFriends);
        List<User> result = new ArrayList<>();
        for (Long id : firstUserFriends) {
            result.add(userRepository.getById(id));
        }
        return result;
    }

    private void checkUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
