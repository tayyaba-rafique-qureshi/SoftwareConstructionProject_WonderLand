package com.wonderland.services;

import java.util.List;

import com.wonderland.models.User;

/**
 * User Service Interface
 * Low Coupling: Provides abstraction for user operations
 */
public interface IUserService {
    /**
     * Get user by username
     * @param username Username
     * @return User object or null if not found
     */
    User getUserByUsername(String username);
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return User object or null if not found
     */
    User getUserById(int userId);
    
    /**
     * Register new user
     * @param user User object with registration details
     * @return true if registration successful
     */
    boolean registerUser(User user);
    
    /**
     * Authenticate user
     * @param username Username
     * @param password Password
     * @return User object if authentication successful, null otherwise
     */
    User authenticateUser(String username, String password);
    
    /**
     * Update user profile
     * @param user User object with updated details
     * @return true if update successful
     */
    boolean updateUser(User user);

    List<String> getSubscribedEmails(); // New Method

    // NEW: Reset password method
    boolean updatePassword(String email, String newPassword);
}
