package com.wonderland.services;

import java.util.List;

import com.wonderland.models.Order;

public interface IEmailService {
    void sendWelcomeEmail(String to, String name, boolean isSubscribed);
    boolean sendOrderConfirmationEmail(Order order);
    boolean sendOrderStatusUpdateEmail(Order order, String status);
    void broadcastSale(String subject, String content, List<String> subscribers);
    void sendPasswordChangeSuccess(String to);
    void sendPasswordChangeAlert(String to);
    
    // --- NEW: Admin Alert ---
    void sendLowStockAlert(int toyId, String toyName, int remainingStock);
}