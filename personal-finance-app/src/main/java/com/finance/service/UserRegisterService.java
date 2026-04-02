package com.finance.service;

import com.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserRegisterService {

    @Autowired
    private UserRepository userRepository;

    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
