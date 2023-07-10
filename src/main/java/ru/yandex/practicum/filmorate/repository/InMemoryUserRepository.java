package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryUserRepository implements CrudRepository<User> {
    private final Map<Long, User> users = new HashMap<>();
    private Long nextUserId = 1L;

    public Map<Long, User> getUsers() {
        return users;
    }

    @Override
    public User create(User user) {
        User newUser = user
                .toBuilder()
                .id(nextUserId++)
                .friends(new HashSet<>())
                .build();

        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public List<User> read() {
        return List.copyOf(users.values());
    }

    @Override
    public User update(User user) {
        User existingUser = users.get(user.getId());
        if (existingUser == null) {
            throw new ObjectNotFoundException("Несуществующий id пользователя: " + user.getId());
        }
        existingUser.setEmail(user.getEmail());
        existingUser.setLogin(user.getLogin());
        existingUser.setName(user.getName());
        existingUser.setBirthday(user.getBirthday());
        return existingUser;
    }

    @Override
    public User getById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new ObjectNotFoundException("Несуществующий id пользователя: " + id);
        }
        return user;
    }
}
