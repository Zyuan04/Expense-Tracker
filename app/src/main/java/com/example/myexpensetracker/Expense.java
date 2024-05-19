package com.example.myexpensetracker;

import com.google.firebase.Timestamp;

public class Expense {
    private String expenseId;
    private String note;
    private double amount;
    private Timestamp date;
    private String categoryId;

    public Expense() {
    }

    public Expense(String expenseId, String note, double amount, Timestamp date, String categoryId) {
        this.expenseId = expenseId;
        this.note = note;
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}


