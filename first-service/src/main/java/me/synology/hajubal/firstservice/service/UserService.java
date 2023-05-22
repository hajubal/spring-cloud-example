package me.synology.hajubal.firstservice.service;

import me.synology.hajubal.firstservice.entity.User;
import me.synology.hajubal.firstservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void deleteUserByUsername(String username) {
        userRepository.deleteByUsername(username);
    }

    // ...
}