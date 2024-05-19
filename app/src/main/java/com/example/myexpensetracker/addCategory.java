package com.example.myexpensetracker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class addCategory extends AppCompatActivity {
    int[] colors = {
            Color.parseColor("#808080"), Color.parseColor("#00827f"), Color.parseColor("#1ca9c9"),
            Color.parseColor("#3aa8c1"), Color.parseColor("#00cc99"), Color.parseColor("#0e7a0d"),
            Color.parseColor("#4682bf"), Color.parseColor("#4169e1"), Color.parseColor("#6a0dad"),
            Color.parseColor("#b86fe5"), Color.parseColor("#cc00cc"), Color.parseColor("#d00060"),
            Color.parseColor("#891446"), Color.parseColor("#ff355e"), Color.parseColor("#ff4500"),
            Color.parseColor("#fb0081"), Color.parseColor("#f58f84"), Color.parseColor("#ff8c00"),
            Color.parseColor("#ffc922"), Color.parseColor("#65ff00")
    };
    View selectedColorView;
    EditText categoryNameEditText;
    private int selectedColorPosition = 0;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        GridView grid = findViewById(R.id.gridView);
        selectedColorView = findViewById(R.id.selectedColorView);
        categoryNameEditText = findViewById(R.id.categoryName);

        selectedColorView.setBackgroundColor(colors[selectedColorPosition]);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        grid.setAdapter(new ExpenseColorAdapter(this));

        Toolbar toolbar = findViewById(R.id.my_toolBar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, Homepage.class));
            finish();
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedColorView.setBackgroundColor(colors[position]);
                selectedColorPosition = position;
            }
        });

        Button btnAddCategory = findViewById(R.id.buttonAddCategory);

        btnAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String categoryName = categoryNameEditText.getText().toString().trim();

                if (categoryName.isEmpty()) {
                    categoryNameEditText.setError("Category name is required");
                } else {
                    categoryNameEditText.setError(null);

                    saveColorToFirestore(colors[selectedColorPosition]);

                    Intent intent = new Intent(addCategory.this, Homepage.class);
                    intent.putExtra("selectedColor", colors[selectedColorPosition]);
                    startActivity(intent);
                    finish();
                }
            }
        });
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

    public class ExpenseColorAdapter extends BaseAdapter {
        private Context context;
        private static final String TAG = "ExpenseColorAdapter";
        public ExpenseColorAdapter(Context c) {
            context = c;
        }

        @Override
        public int getCount() {
            return colors.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CardView cardView;
            if (convertView == null) {
                cardView = new CardView(context);
                cardView.setLayoutParams(new GridView.LayoutParams(150, 150));
                cardView.setRadius(30);

                ImageView colorView = new ImageView(context);
                colorView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                colorView.setBackgroundColor(colors[position]);

                cardView.addView(colorView);
            } else {
                cardView = (CardView) convertView;
            }

            ImageView colorView = (ImageView) cardView.getChildAt(0);
            colorView.setBackgroundColor(colors[position]);

            if (position == selectedColorPosition) {
                selectedColorView.setBackgroundColor(colors[position]);
            }

            return cardView;
        }
    }
    private void saveColorToFirestore(int selectedColor) {
        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> categoryData = new HashMap<>();

        categoryData.put("selectedColor", selectedColor);
        categoryData.put("categoryName", categoryNameEditText.getText().toString());
        categoryData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("expenses")
                .document(userId)
                .collection("user_categories")
                .add(categoryData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Color added with ID: " + documentReference.getId());

                    String categoryId = documentReference.getId();
                    retrieveUserIdAndCategoryId(userId, categoryId);
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding color", e);
                });
    }

    private void retrieveUserIdAndCategoryId(String userId, String categoryId) {
        Log.d("Firestore", "Retrieved userId: " + userId + ", categoryId: " + categoryId);
    }
}