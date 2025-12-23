package com.wonderland.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wonderland.models.User;
import com.wonderland.services.EmailService;
import com.wonderland.services.IUserService; // Import the interface

@Controller
public class RegisterController {

    @Autowired
    private IUserService userService; // LOW COUPLING: Using the interface type

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    @ResponseBody
    public String register(@RequestParam String username, 
                           @RequestParam String firstname, 
                           @RequestParam String lastname, 
                           @RequestParam String email, 
                           @RequestParam String password, 
                           @RequestParam(required = false) String subscribe) {
        
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setFirstName(firstname);
        newUser.setLastName(lastname);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setNewsletterSubscribed(subscribe != null);

        try {
            // CALL UPDATED METHOD NAME: registerUser
            if (userService.registerUser(newUser)) {
                emailService.sendWelcomeEmail(newUser.getEmail(), newUser.getFirstName(), newUser.isNewsletterSubscribed());
                return "<script>alert('Account Created!'); window.location='/index.html';</script>";
            }
        } catch (Exception e) {
            return "<h3>Error: " + e.getMessage() + "</h3>";
        }
        return "<h3>Could not create account. Please try again.</h3>";
    }
}