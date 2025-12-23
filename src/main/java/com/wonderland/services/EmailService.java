package com.wonderland.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.wonderland.models.Order;
import com.wonderland.models.OrderItem;

@Service
public class EmailService implements IEmailService {

    @Autowired
    private JavaMailSender mailSender;
   

    // Pull from address from properties for better modularity
    @Value("${spring.mail.username:tayyabarafique204@gmail.com}")
    private String fromEmail;

    @Override
    public void sendWelcomeEmail(String to, String name, boolean isSubscribed) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome to Wonderland, " + name + "! ✨");
            
            String content = "Welcome to Wonderland Toystore! We're thrilled to have you join our community.\n\n" +
                             "Start exploring our magical collection here: http://localhost:8081/shop.html";
            
            if (isSubscribed) {
                content += "\n\nPS: You've successfully subscribed to our newsletter for magical deals!";
            }
            
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            // ROBUSTNESS: Log error but don't crash registration process
            System.err.println("⚠️ Warning: Could not send welcome email: " + e.getMessage());
        }
    }

    @Override
    public boolean sendOrderConfirmationEmail(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(order.getEmail());
            message.setSubject("Order Confirmed - #" + order.getId() + " 🛍️");

            StringBuilder body = new StringBuilder();
            body.append("Thank you for your order, ").append(order.getFirstname()).append("!\n\n");
            body.append("Your order has been received and is currently ").append(order.getOrderStatus()).append(".\n\n");
            
            body.append("--- Order Details ---\n");
            for (OrderItem item : order.getItems()) {
                body.append("- ").append(item.getToyName() != null ? item.getToyName() : "Toy ID: " + item.getToyId())
                    .append(" x").append(item.getQuantity())
                    .append(" (Rs ").append(String.format("%.2f", item.getPrice())).append(")\n");
            }
            
            body.append("\nSubtotal: Rs ").append(String.format("%.2f", order.getSubtotal()));
            body.append("\nShipping: Rs ").append(String.format("%.2f", order.getShippingCost()));
            body.append("\nTotal Amount: Rs ").append(String.format("%.2f", order.getTotal()));
            
            body.append("\n\n--- Shipping To ---\n");
            body.append(order.getAddress()).append("\n")
                .append(order.getCity()).append(", ").append(order.getPostalCode());

            message.setText(body.toString());
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("⚠️ Error sending order confirmation: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendOrderStatusUpdateEmail(Order order, String status) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(order.getEmail());
            message.setSubject("Update: Your Order #" + order.getId() + " is " + status);
            
            message.setText("Hello " + order.getFirstname() + ",\n\n" +
                            "We wanted to let you know that your order status has been updated to: " + status + ".\n\n" +
                            "Track your order here: http://localhost:8081/orders.html");
            
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("⚠️ Error sending status update: " + e.getMessage());
            return false;
        }
    }

    // Inside EmailService.java
// Inside EmailService.java - NO UserService injection needed

public void broadcastSale(String subject, String content, List<String> subscribers) {
    for (String email : subscribers) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tayyabarafique204@gmail.com");
            message.setTo(email);
            message.setSubject("🔥 " + subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to email: " + email);
        }
    }
}
@Override
    public void sendPasswordChangeSuccess(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Security Update: Password Changed Successfully ✅");
        message.setText("Hello,\n\n" +
                        "Your Wonderland account password was successfully changed.\n" +
                        "If you did not make this change, please contact support immediately.\n\n" +
                        "Stay Magical! ✨");
        mailSender.send(message);
    }

    @Override
    public void sendPasswordChangeAlert(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("⚠️ Security Alert: Failed Password Change Attempt");
        message.setText("Hello,\n\n" +
                        "We noticed an attempt to change your password, but the 'Old Password' provided was incorrect.\n" +
                        "For your security, the password has NOT been changed.\n\n" +
                        "If this was you, please try again with the correct password.\n" +
                        "If this wasn't you, we recommend securing your account immediately.");
        mailSender.send(message);
    }

    // Wonderland/src/main/java/com/wonderland/services/EmailService.java

// ... existing imports ...

    // ... existing methods ...
// Wonderland/src/main/java/com/wonderland/services/EmailService.java

@Override
public void sendLowStockAlert(int toyId, String toyName, int remainingStock) {
    System.out.println("📧 ATTEMPTING to send email for: " + toyName); // DEBUG LOG

    try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo("tayyabarafique204@gmail.com"); // TEMPORARY: Hardcode a DIFFERENT email to test reception
        message.setSubject("TEST ALERT: " + toyName);
        message.setText("Stock is low: " + remainingStock);
        
        mailSender.send(message);
        System.out.println("✅ Email sent successfully!");
    } catch (Exception e) {
        System.err.println("❌ EMAIL FAILED: " + e.getMessage());
        e.printStackTrace(); // Print full error to see why
    }
}}