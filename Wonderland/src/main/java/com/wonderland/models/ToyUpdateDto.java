package com.wonderland.models;

public class ToyUpdateDto {
    private String name;
    private String brand;
    private String category;
    private Double price;
    private Integer minAge;
    private String targetAudience;
    private String description;
    // Add this field
    private Integer stockQuantity; 

    public ToyUpdateDto() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getMinAge() { return minAge; }
    public void setMinAge(Integer minAge) { this.minAge = minAge; }
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    // Add Getter and Setter for stockQuantity
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}