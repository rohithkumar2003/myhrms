package com.example.demo.service;

import com.example.demo.model.User;

import java.util.Optional;

public interface UserDetailsService {
    User saveUser(User user);
    Optional<User> findByEmail(String email);
}
