package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.User;
import com.smartpdf.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user) {

        return userRepository.save(user);

    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    public User updateUser(Long id, User userDetails) {

        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setPassword(userDetails.getPassword());

            return userRepository.save(user);
        }

        return null;
    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

}