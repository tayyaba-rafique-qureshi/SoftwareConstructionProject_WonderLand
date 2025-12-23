package com.wonderland.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.wonderland.models.User;

@Service
public class UserService implements IUserService { // ABSTRACTION: Implementing the interface

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean registerUser(User user) { // RENAMED: To match IUserService interface
        String sql = "INSERT INTO users (username, firstname, lastname, email, password, newsletter_subscribed, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'CUSTOMER')";
        
        int rows = jdbcTemplate.update(sql, 
            user.getUsername(), 
            user.getFirstName(), 
            user.getLastName(), 
            user.getEmail(), 
            user.getPassword(), 
            user.isNewsletterSubscribed());
            
        return rows > 0;
    }

    @Override
    public User authenticateUser(String email, String password) { // RENAMED: To match interface
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        List<User> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), email, password);
        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), username);
        return users.isEmpty() ? null : users.get(0);
    }

    // Add empty implementations for other IUserService methods to satisfy the contract
    @Override public User getUserById(int userId) { return null; }
    @Override public boolean updateUser(User user) { return false; }

    // Wonderland/src/main/java/com/wonderland/services/UserService.java

@Override
public List<String> getSubscribedEmails() {
    String sql = "SELECT email FROM users WHERE newsletter_subscribed = TRUE";
    // Fetch list of strings directly
    return jdbcTemplate.queryForList(sql, String.class);
}
@Override
    public boolean updatePassword(String email, String newPassword) {
        // Robustness: SQL Update to change password
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        int rows = jdbcTemplate.update(sql, newPassword, email);
        return rows > 0;
    }
}