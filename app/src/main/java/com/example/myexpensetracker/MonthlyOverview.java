package com.example.myexpensetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MonthlyOverview extends AppCompatActivity implements OverviewListAdapter.OnItemClickListener {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private RecyclerView recyclerView;
    private OverviewListAdapter expenseListAdapter;
    private List<MonthlyItem> monthlyItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_overview);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        monthlyItemList = new ArrayList<>();
        expenseListAdapter = new OverviewListAdapter(this, monthlyItemList);
        recyclerView.setAdapter(expenseListAdapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        expenseListAdapter.setOnItemClickListener(this);

        Toolbar toolbar = findViewById(R.id.my_toolBar);
        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, Homepage.class));
            finish();
        });

        retrieveAndCalculateMonthlyTotal();
    }

    private void retrieveAndCalculateMonthlyTotal() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        db.collection("expenses").document(userId).collection("user_categories")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Double> monthlyTotalAmounts = new HashMap<>();

                    for (QueryDocumentSnapshot categoryDocument : queryDocumentSnapshots) {
                        String categoryId = categoryDocument.getId();

                        db.collection("expenses").document(userId).collection("user_categories")
                                .document(categoryId).collection("user_expenses")
                                .get()
                                .addOnSuccessListener(expenseDocumentSnapshots -> {
                                    for (QueryDocumentSnapshot expenseDocument : expenseDocumentSnapshots) {
                                        Expense expense = expenseDocument.toObject(Expense.class);

                                        Calendar expenseCalendar = Calendar.getInstance();
                                        expenseCalendar.setTimeInMillis(expense.getDate().getSeconds() * 1000);

                                        int month = expenseCalendar.get(Calendar.MONTH) + 1;

                                        double expenseAmount = expense.getAmount();
                                        monthlyTotalAmounts.merge(getMonthName(month), expenseAmount, Double::sum);
                                    }

                                    monthlyItemList.clear();
                                    for (Map.Entry<String, Double> entry : monthlyTotalAmounts.entrySet()) {
                                        String monthYear = String.format(Locale.getDefault(), "%s %d", entry.getKey(), currentYear);
                                        monthlyItemList.add(new MonthlyItem(categoryId, monthYear, entry.getValue()));
                                    }
                                    expenseListAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error retrieving user expenses for category " + categoryId, e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error retrieving user categories", e);
                });
    }

    private String getMonthName(int month) {
        String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return monthNames[month - 1];
    }

    @Override
    public void onItemClick(String monthName) {
        String[] parts = monthName.split(" ");
        String month = parts[0];
        int year = Integer.parseInt(parts[1]);

        Intent intent = new Intent(this, ExpenseDetails.class);
        intent.putExtra("selectedMonth", month);
        intent.putExtra("selectedYear", year);
        startActivity(intent);
    }
}
