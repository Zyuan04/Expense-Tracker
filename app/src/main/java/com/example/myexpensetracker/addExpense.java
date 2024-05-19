package com.example.myexpensetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class addExpense extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText noteEditText, amountEditText;
    private TextView dateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        noteEditText = findViewById(R.id.editTextNote);
        amountEditText = findViewById(R.id.editTextAmount);
        dateTextView = findViewById(R.id.textViewDate);

        Toolbar toolbar = findViewById(R.id.my_toolBar);
        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, Homepage.class));
            finish();
        });

        dateTextView.setOnClickListener(v -> showDatePicker());

        Button pickDateButton = findViewById(R.id.buttonPickDate);
        pickDateButton.setOnClickListener(v -> showDatePicker());

        Button addExpenseButton = findViewById(R.id.buttonAddExpense);
        addExpenseButton.setOnClickListener(v -> addExpense());
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, Homepage.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            if (dateTextView != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String selectedDate = dateFormat.format(calendar.getTime());

                dateTextView.setText(selectedDate);

                try {
                    Date date = parseDate(selectedDate);
                    if (date == null) {
                        throw new ParseException("Date parsing failed", 0);
                    }
                    dateTextView.setText(selectedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                    dateTextView.setError("Invalid date format");
                }
            }
        };

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
        datePickerDialog.show();

    }

    private Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void addExpense() {
        String note = noteEditText.getText().toString().trim();
        String amountString = amountEditText.getText().toString().trim();
        String dateString = dateTextView.getText().toString().trim();

        if (note.isEmpty()) {
            noteEditText.setError("Note is required");
            return;
        }
        if (amountString.isEmpty()) {
            amountEditText.setError("Amount is required");
            return;
        }

        if (dateString.isEmpty()) {
            dateTextView.setError("Date is required");
            return;
        }

        try {
            double amount = Double.parseDouble(amountString);

            Date date = parseDate(dateString);

            if (date == null) {
                dateTextView.setError("Invalid date format");
                return;
            }

            Map<String, Object> expenseData = new HashMap<>();
            expenseData.put("note", note);
            expenseData.put("amount", amount);
            expenseData.put("date", date);

            String userId = mAuth.getCurrentUser().getUid();
            String categoryId = getIntent().getStringExtra("categoryId");


            if (categoryId == null || categoryId.isEmpty()) {
                Log.e("addExpense", "Invalid categoryId");
                return;
            }

            DocumentReference categoryRef = db.collection("expenses")
                    .document(userId)
                    .collection("user_categories")
                    .document(categoryId);

            categoryRef.collection("user_expenses")
                    .add(expenseData)
                    .addOnSuccessListener(documentReference -> {
                        noteEditText.setText("");
                        amountEditText.setText("");
                        dateTextView.setText("");

                        String expenseId = documentReference.getId();
                        Intent intent = new Intent(addExpense.this, Homepage.class);
                        intent.putExtra("expenseId", expenseId);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error adding expense", e);
                    });
        } catch (NumberFormatException e) {
            amountEditText.setError("Invalid amount format");
        }
    }
}
