package com.wonderland.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wonderland.models.User;
import com.wonderland.services.IEmailService;
import com.wonderland.services.IUserService;

import jakarta.servlet.http.HttpSession; // Import the interface

@Controller
public class LoginController {

    @Autowired
    private IEmailService emailService;
    @Autowired
    private IUserService userService; // LOW COUPLING: Using the interface type
// Wonderland/src/main/java/com/wonderland/controllers/LoginController.java

@PostMapping("/LoginServlet")
@ResponseBody
public String login(@RequestParam String email, @RequestParam String password, HttpSession session) {
    // Calling the method defined in the interface (Abstraction)
    User user = userService.authenticateUser(email, password);

    if (user != null) {
        // --- 1. PERSIST SESSION (BACKEND) ---
        // We set BOTH keys so that NO page breaks.
        session.setAttribute("user", user.getUsername());     // Original key used by index/shop
        session.setAttribute("username", user.getUsername()); // New key required by CheckoutController
        
        session.setAttribute("role", user.getRole());
        session.setAttribute("email", user.getEmail());

        // --- 2. DETERMINE REDIRECTION ---
        String redirectPage = "index.html";
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            redirectPage = "admin.html";
        }

        // --- 3. PERSIST SESSION (FRONTEND) ---
        // This keeps your script.js and UI updates working
        return "<script>" +
               "sessionStorage.setItem('username', '" + user.getUsername() + "');" +
               "sessionStorage.setItem('userEmail', '" + user.getEmail() + "');" +
               "sessionStorage.setItem('role', '" + user.getRole() + "');" +
               "window.location='" + redirectPage + "';" +
               "</script>";
    } else {
        return "<script>alert('Invalid Login'); window.location='index.html';</script>";
    }
}
    @GetMapping("/logout")
    @ResponseBody
    public String logout(HttpSession session) {
        session.invalidate();
        return "<script>" +
               "sessionStorage.clear();" + 
               "window.location='index.html';" +
               "</script>";
    }

    @PostMapping("/api/change-password")
    @ResponseBody
    public String changePassword(@RequestParam String email, 
                                 @RequestParam String oldPassword, 
                                 @RequestParam String newPassword) {
        
        // 1. Verify Old Password
        User user = userService.authenticateUser(email, oldPassword);

        if (user != null) {
            // --- SCENARIO A: Old Password is VALID ---
            boolean updateSuccess = userService.updatePassword(email, newPassword);
            
            if (updateSuccess) {
                emailService.sendPasswordChangeSuccess(email); // Success Email
                return "{\"success\": true, \"message\": \"Password changed successfully!\"}";
            } else {
                return "{\"success\": false, \"message\": \"Database error occurred.\"}";
            }
        } else {
            // --- SCENARIO B: Old Password is INVALID ---
            // Send Alert Email to the owner of the email address
            emailService.sendPasswordChangeAlert(email); 
            return "{\"success\": false, \"message\": \"Incorrect old password. Security alert sent.\"}";
        }
    }
}