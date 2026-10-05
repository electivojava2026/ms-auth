package com.eventpass.ms_auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eventpass.ms_auth.model.User;
import com.eventpass.ms_auth.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // c
    public User createUser(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")
                && !user.getPassword().startsWith("$2b$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    // r
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // u
    public User updateUser(User user) throws Exception {
        if (!userRepository.existsById(user.getId())) {
            throw new Exception("User not found. Failed to update.");
        }

        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")
                && !user.getPassword().startsWith("$2b$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    // d
    public void deleteUser(User user) throws Exception {
        if (!userRepository.existsById(user.getId())) {
            throw new Exception("User not found. Failed to delete.");
        }

        userRepository.deleteById(user.getId());
    }
}
