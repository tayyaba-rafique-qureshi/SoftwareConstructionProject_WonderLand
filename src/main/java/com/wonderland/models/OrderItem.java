package com.wonderland.models;

/**
 * OrderItem Model - Represents individual items in an order
 * High Cohesion: Handles only order item data
 */
public class OrderItem {
    private int id;
    private int orderId;
    private int toyId;
    private int quantity;
    private double price;
    
    // Optional: For display purposes (not stored in DB)
    private String toyName;
    private String toyImage;

    // Constructors
    public OrderItem() {}

    public OrderItem(int toyId, int quantity, double price) {
        this.toyId = toyId;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getToyId() {
        return toyId;
    }

    public void setToyId(int toyId) {
        this.toyId = toyId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getToyName() {
        return toyName;
    }

    public void setToyName(String toyName) {
        this.toyName = toyName;
    }

    public String getToyImage() {
        return toyImage;
    }

    public void setToyImage(String toyImage) {
        this.toyImage = toyImage;
    }

    // Business method: Calculate item total
    public double getItemTotal() {
        return this.price * this.quantity;
    }
}
