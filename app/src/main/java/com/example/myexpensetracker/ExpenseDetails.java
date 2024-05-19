package com.example.myexpensetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseDetails extends AppCompatActivity implements ExpenseAdapter.OnExpenseItemClickListener {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private RecyclerView recyclerView;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;
    private TextView toolbarTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        toolbarTitleTextView = findViewById(R.id.toolbar_title);

        Toolbar toolbar = findViewById(R.id.my_toolBar);
        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, MonthlyOverview.class));
            finish();
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(this,expenseList);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(expenseAdapter);
        expenseAdapter.setOnExpenseItemClickListener(this);

        String monthName = getIntent().getStringExtra("selectedMonth");
        int year = getIntent().getIntExtra("selectedYear", 0);

        toolbarTitleTextView.setText(String.format(Locale.getDefault(), "%s %d", monthName, year));

        retrieveExpensesForMonth(monthName, year);
    }

    private void retrieveExpensesForMonth(String monthName, int year) {
        expenseList.clear();
        int monthIndex = getMonthIndex(monthName);
        if (monthIndex == -1) {
            Log.e("ExpenseDetails", "Invalid month name: " + monthName);
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthIndex);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timestamp startOfMonth = new Timestamp(calendar.getTime());
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        Timestamp endOfMonth = new Timestamp(calendar.getTime());

        db.collection("expenses").document(userId).collection("user_categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot categoryDocument : queryDocumentSnapshots) {
                        String categoryId = categoryDocument.getId();
                        db.collection("expenses").document(userId).collection("user_categories")
                                .document(categoryId).collection("user_expenses")
                                .orderBy("date", Query.Direction.DESCENDING)
                                .whereGreaterThanOrEqualTo("date", startOfMonth)
                                .whereLessThanOrEqualTo("date", endOfMonth)
                                .get()
                                .addOnSuccessListener(expenseDocumentSnapshots -> {
                                    List<Expense> expenses = new ArrayList<>();
                                    for (QueryDocumentSnapshot expenseDocument : expenseDocumentSnapshots) {
                                        Expense expense = expenseDocument.toObject(Expense.class);
                                        expense.setExpenseId(expenseDocument.getId());
                                        expenses.add(expense);
                                        expense.setCategoryId(categoryId);
                                    }

                                    Log.d("Firestore", "Number of expenses retrieved: " + expenses.size());

                                    for (Expense expense : expenses) {
                                        Log.d("Firestore", "Retrieved Expense:");
                                        Log.d("Firestore", "Amount: " + expense.getAmount());
                                        Log.d("Firestore", "Note: " + expense.getNote());
                                        Log.d("Firestore", "Date: " + expense.getDate());
                                    }
                                    expenseList.addAll(expenses);
                                    expenseAdapter.notifyDataSetChanged();

                                    Log.d("Firestore", "User expenses retrieved successfully for month " + monthName + " and year " + year);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error fetching user expenses for month " + monthName, e);
                                });
                    }
                });
    }

    private int getMonthIndex(String monthName) {
        String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        for (int i = 0; i < monthNames.length; i++) {
            if (monthNames[i].equalsIgnoreCase(monthName)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onItemClick(Expense expense) {
        Intent intent = new Intent(this, editExpense.class);
        intent.putExtra("note", expense.getNote());
        intent.putExtra("amount", expense.getAmount());
        intent.putExtra("date", expense.getDate().toDate().getTime());
        intent.putExtra("expenseId", expense.getExpenseId());
        intent.putExtra("categoryId", expense.getCategoryId());

        String month = getIntent().getStringExtra("selectedMonth");
        int year = getIntent().getIntExtra("selectedYear", 0);

        intent.putExtra("selectedMonth", month);
        intent.putExtra("selectedYear", year);
        startActivity(intent);
    }
}
