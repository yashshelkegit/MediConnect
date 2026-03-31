package com.cts.mediconnect.service;

import com.cts.mediconnect.model.User;
import com.cts.mediconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // CREATE
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // READ ALL
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // READ BY ID
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    // UPDATE
    public User updateUser(Integer id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setName(updatedUser.getName());
            user.setEmail(updatedUser.getEmail());
            user.setPassword(updatedUser.getPassword());
            user.setPhone(updatedUser.getPhone());
            user.setRole(updatedUser.getRole());
            user.setHospital(updatedUser.getHospital());
            user.setSpecialization(updatedUser.getSpecialization());
            user.setAvailabilityStatus(updatedUser.getAvailabilityStatus());
            user.setDateOfBirth(updatedUser.getDateOfBirth());
            user.setGender(updatedUser.getGender());
            user.setBloodGroup(updatedUser.getBloodGroup());
            user.setAddress(updatedUser.getAddress());
            user.setEmergencyContact(updatedUser.getEmergencyContact());

            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // DELETE
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}