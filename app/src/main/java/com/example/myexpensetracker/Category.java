package com.example.myexpensetracker;

import com.google.firebase.Timestamp;

public class Category {
    private String categoryId;
    private String categoryName;
    private Timestamp timestamp;
    private int selectedColor;

    public Category() {

    }

    public Category(String categoryId, String categoryName, Timestamp timestamp, int selectedColor) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.timestamp = timestamp;
        this.selectedColor = selectedColor;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }
}