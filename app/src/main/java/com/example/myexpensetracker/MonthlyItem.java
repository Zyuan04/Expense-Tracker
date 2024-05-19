package com.example.myexpensetracker;

public class MonthlyItem {
    private String categoryId;
    private String monthYear;
    private double expenseAmount;

    public MonthlyItem(String categoryId, String monthYear, double expenseAmount) {
        this.categoryId = categoryId;
        this.monthYear = monthYear;
        this.expenseAmount = expenseAmount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public double getExpenseAmount() {
        return expenseAmount;
    }
}