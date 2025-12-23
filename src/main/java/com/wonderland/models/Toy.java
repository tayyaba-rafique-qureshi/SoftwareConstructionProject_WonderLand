package com.wonderland.models;

/**
 * Abstraction: This class serves as the blueprint for all toy types.
 * Encapsulation: All fields are private, protecting data integrity.
 */
public class Toy { // Removed 'abstract' so Spring can create Toy objects
    private Integer id;
    private String name;
    private Double price;
    private String brand;
    private Integer minAge;
    private String targetAudience;
    private String category;
    private String imageUrl;
    private Integer stockQuantity;
    private String description;
    
private Double salePrice;
private boolean isOnSale;

    // Inside com.wonderland.models.Toy
private String itemType; // Physical or Digital

    // Default Constructor: Required by Spring/JDBC for object creation
    public Toy() {}
    // Inside com.wonderland.models.Toy.java

// Add Getters and Setters
public Double getSalePrice() { return salePrice; }
public void setSalePrice(Double salePrice) { this.salePrice = salePrice; }
public boolean isOnSale() { return isOnSale; }
public void setOnSale(boolean onSale) { isOnSale = onSale; }
    // Encapsulated Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Integer getMinAge() { return minAge; }
    public void setMinAge(Integer minAge) { this.minAge = minAge; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

public String getItemType() { return itemType; }
public void setItemType(String itemType) { this.itemType = itemType; }
    /**
     * Virtual Method: Instead of abstract, we provide a default implementation.
     * This fulfills the requirement for "varying attributes" because 
     * subclasses (like VideoGame) can override this.
     */
    public String getUniqueAttributes() {
        return "Standard Category: " + this.category;
    }
}