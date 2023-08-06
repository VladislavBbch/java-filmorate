package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.FriendRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

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
    }

    public void deleteFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);
        friendRepository.deleteFriend(userId, friendId);
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

    private void checkUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

}
