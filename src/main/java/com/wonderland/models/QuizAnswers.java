// Wonderland/src/main/java/com/wonderland/models/QuizAnswers.java
package com.wonderland.models;

public class QuizAnswers {
    private String ageGroup; // Changed from int to String (e.g., "3-5")
    private double budget;
    private String interest;

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getInterest() { return interest; }
    public void setInterest(String interest) { this.interest = interest; }
}