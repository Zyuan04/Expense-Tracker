package com.example.myexpensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class editExpense extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText editTextNote;
    private EditText editTextAmount;
    private TextView textViewDate;
    private Button buttonEditExpense;
    private Button buttonDeleteExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        editTextNote = findViewById(R.id.editTextNote);
        editTextAmount = findViewById(R.id.editTextAmount);
        textViewDate = findViewById(R.id.textViewDate);
        buttonEditExpense = findViewById(R.id.buttonEditExpense);
        buttonDeleteExpense = findViewById(R.id.buttonDeleteExpense);

        Toolbar toolbar = findViewById(R.id.my_toolBar);
        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(editExpense.this, ExpenseDetails.class);
            String month = getIntent().getStringExtra("selectedMonth");
            int year = getIntent().getIntExtra("selectedYear", 0);

            intent.putExtra("selectedMonth", month);
            intent.putExtra("selectedYear", year);
            startActivity(intent);
            finish();
        });

        String note = getIntent().getStringExtra("note");
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        long dateInMillis = getIntent().getLongExtra("date", 0);
        String expenseId = getIntent().getStringExtra("expenseId");
        String categoryId = getIntent().getStringExtra("categoryId");

        if (note != null && !note.isEmpty()) {
            editTextNote.setText(note);
        }

        if (amount != 0.0) {
            editTextAmount.setText(String.valueOf(amount));
        }

        if (dateInMillis != 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = new Date(dateInMillis);
            String formattedDate = dateFormat.format(date);
            textViewDate.setText(formattedDate);
        }

        textViewDate.setOnClickListener(v -> showDatePicker());

        Button pickDateButton = findViewById(R.id.buttonPickDate);
        pickDateButton.setOnClickListener(v -> showDatePicker());

        Button buttonEditExpense = findViewById(R.id.buttonEditExpense);
        buttonEditExpense.setOnClickListener(v -> saveExpense());

        Button buttonDeleteExpense = findViewById(R.id.buttonDeleteExpense);
        buttonDeleteExpense.setOnClickListener(v -> deleteExpense());
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            if (textViewDate != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String selectedDate = dateFormat.format(calendar.getTime());

                textViewDate.setText(selectedDate);

                try {
                    Date date = parseDate(selectedDate);
                    if (date == null) {
                        throw new ParseException("Date parsing failed", 0);
                    }
                    textViewDate.setText(selectedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                    textViewDate.setError("Invalid date format");
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

    private void saveExpense() {
        String note = editTextNote.getText().toString().trim();
        String amountString = editTextAmount.getText().toString().trim();
        String dateString = textViewDate.getText().toString().trim();

        if (note.isEmpty()) {
            editTextNote.setError("Note is required");
            return;
        }
        if (amountString.isEmpty()) {
            editTextAmount.setError("Amount is required");
            return;
        }

        if (dateString.isEmpty()) {
            textViewDate.setError("Date is required");
            return;
        }

        try {
            double amount = Double.parseDouble(amountString);

            Date date = parseDate(dateString);

            if (date == null) {
                textViewDate.setError("Invalid date format");
                return;
            }

            Map<String, Object> updatedExpenseData = new HashMap<>();
            updatedExpenseData.put("note", note);
            updatedExpenseData.put("amount", amount);
            updatedExpenseData.put("date", date);

            String expenseId = getIntent().getStringExtra("expenseId");
            String categoryId = getIntent().getStringExtra("categoryId");

            if (expenseId == null || expenseId.isEmpty()) {
                Log.e("editExpense", "Invalid expenseId");
                return;
            }

            DocumentReference expenseRef = db.collection("expenses")
                    .document(mAuth.getCurrentUser().getUid())
                    .collection("user_categories")
                    .document(categoryId)
                    .collection("user_expenses")
                    .document(expenseId);

            expenseRef.update(updatedExpenseData)
                    .addOnSuccessListener(aVoid -> {
                        editTextNote.setText("");
                        editTextAmount.setText("");
                        textViewDate.setText("");

                        Intent intent = new Intent(editExpense.this, ExpenseDetails.class);
                        String month = getIntent().getStringExtra("selectedMonth");
                        int year = getIntent().getIntExtra("selectedYear", 0);

                        intent.putExtra("selectedMonth", month);
                        intent.putExtra("selectedYear", year);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error updating expense", e);
                    });
        } catch (NumberFormatException e) {
            editTextAmount.setError("Invalid amount format");
        }
    }

    private void deleteExpense() {
        String expenseId = getIntent().getStringExtra("expenseId");
        String categoryId = getIntent().getStringExtra("categoryId");
        DocumentReference expenseRef = db.collection("expenses")
                .document(mAuth.getCurrentUser().getUid())
                .collection("user_categories")
                .document(categoryId)
                .collection("user_expenses")
                .document(expenseId);

        expenseRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Expense deleted with ID: " + expenseId);
                        Intent intent = new Intent(editExpense.this, ExpenseDetails.class);
                        String month = getIntent().getStringExtra("selectedMonth");
                        int year = getIntent().getIntExtra("selectedYear", 0);

                        intent.putExtra("selectedMonth", month);
                        intent.putExtra("selectedYear", year);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error deleting expense", e);
                    }
                });
    }
}
