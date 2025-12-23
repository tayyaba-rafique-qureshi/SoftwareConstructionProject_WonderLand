package com.wonderland.models;

import java.time.LocalDateTime;

/**
 * Represents a single item within a user's shopping cart.
 * Encapsulates a Toy object and the quantity selected.
 */
public class CartItem {
    private Long id;
    private Long userId;
    private Long toyId;
    private Toy toy; // For display purposes
    private Integer quantity;
    private LocalDateTime addedAt;
    
    // Constructors
    public CartItem() {}
    
    public CartItem(Long userId, Long toyId, Integer quantity) {
        this.userId = userId;
        this.toyId = toyId;
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getToyId() { return toyId; }
    public void setToyId(Long toyId) { this.toyId = toyId; }
    
    public Toy getToy() { return toy; }
    public void setToy(Toy toy) { this.toy = toy; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
    
    // Helper for total price
    public double getTotalPrice() {
        return toy.getPrice() * quantity;
    }
}