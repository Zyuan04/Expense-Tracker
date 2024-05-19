package com.example.myexpensetracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Homepage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private String userId;
    private CategoryAdapter categoryAdapter;
    private TextView expenseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        expenseTextView = findViewById(R.id.expenseTextView);
        recyclerView = findViewById(R.id.categoryRecyclerView);

        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // Format the date to display month and year
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(calendar.getTime());

        // Display the formatted date in a TextView
        TextView dateView = findViewById(R.id.dateTextView);
        dateView.setText(formattedDate);

        userId = mAuth.getCurrentUser().getUid();

        FirestoreRecyclerOptions<Category> options = getCategoryRecyclerOptions();
        categoryAdapter = new CategoryAdapter(options);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(categoryAdapter);

        Button addCategoryButton = findViewById(R.id.addCategoryButton);

        Toolbar toolbar = findViewById(R.id.my_toolBar);

        DrawerLayout drawerLayout=findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(
                this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close
        );

        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, android.R.color.black));

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView=findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        categoryAdapter.setOnCategoryClickListener(new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(DocumentSnapshot documentSnapshot, int position) {
                if (position != RecyclerView.NO_POSITION) {
                    String categoryId = documentSnapshot.getId();

                    Category clickedCategory = documentSnapshot.toObject(Category.class);

                    Intent intent = new Intent(Homepage.this, addExpense.class);
                    intent.putExtra("categoryId", categoryId);
                    intent.putExtra("categoryName", clickedCategory.getCategoryName());
                    startActivity(intent);
                }
            }
        });

        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Homepage.this,addCategory.class));
            }
        });

        loadCategoriesFromFirestore();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Override
    protected void onStart() {
        super.onStart();
        categoryAdapter.startListening();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d("NavigationItemSelected", "Item selected: " + item.getItemId());

        displaySelectedListener(item.getItemId());
        return true;
    }
    public void displaySelectedListener(int itemId){
        DrawerLayout drawerLayout=findViewById(R.id.drawer_layout);

        if (itemId == R.id.homepage) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else if(itemId == R.id.month_overview){
            Intent intent = new Intent(Homepage.this, MonthlyOverview.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if (itemId == R.id.logout) {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(Homepage.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void loadCategoriesFromFirestore() {
        FirestoreRecyclerOptions<Category> options = getCategoryRecyclerOptions();
        categoryAdapter.updateOptions(options);
        calculateTotalAmount();
    }

    private FirestoreRecyclerOptions<Category> getCategoryRecyclerOptions() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar months are 0-based

        // Set the calendar to the start of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timestamp startOfMonth = new Timestamp(calendar.getTime());

        // Set the calendar to the end of the month
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1); // Set to the last millisecond of the month
        Timestamp endOfMonth = new Timestamp(calendar.getTime());

        Query query = db.collection("expenses").document(userId)
                .collection("user_categories")
                .whereGreaterThanOrEqualTo("timestamp", startOfMonth)
                .whereLessThanOrEqualTo("timestamp", endOfMonth)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        return new FirestoreRecyclerOptions.Builder<Category>()
                .setQuery(query, Category.class)
                .build();
    }

    private void calculateTotalAmount() {
        String userId = mAuth.getCurrentUser().getUid();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar months are 0-based

        // Set the calendar to the start of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timestamp startOfMonth = new Timestamp(calendar.getTime());

        // Set the calendar to the end of the month
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1); // Set to the last millisecond of the month
        Timestamp endOfMonth = new Timestamp(calendar.getTime());

        db.collection("expenses").document(userId).collection("user_categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double[] totalAmount = {0.0};

                    categoryAdapter.clearData();

                    for (QueryDocumentSnapshot categoryDocument : queryDocumentSnapshots) {
                        String categoryId = categoryDocument.getId();
                        db.collection("expenses").document(userId).collection("user_categories")
                                .document(categoryId).collection("user_expenses")
                                .whereGreaterThanOrEqualTo("date", startOfMonth)
                                .whereLessThanOrEqualTo("date", endOfMonth)
                                .get()
                                .addOnSuccessListener(expenseDocumentSnapshots -> {
                                    for (QueryDocumentSnapshot expenseDocument : expenseDocumentSnapshots) {
                                        Expense expense = expenseDocument.toObject(Expense.class);
                                        totalAmount[0] += expense.getAmount();
                                    }
                                    // Ensure the adapter is still valid and the position is within bounds
                                    if (categoryAdapter != null && categoryAdapter.getItemCount() > 0) {
                                        runOnUiThread(() -> updateExpenseTextView(totalAmount[0]));
                                    }
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

    private void updateExpenseTextView(double totalAmount) {
        if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
            expenseTextView.setText(String.format(Locale.getDefault(), "RM %.2f", totalAmount));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        categoryAdapter.stopListening();
    }
}
