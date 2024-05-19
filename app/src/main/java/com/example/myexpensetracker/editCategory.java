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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class editCategory extends AppCompatActivity {
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
    private int selectedColorPosition=-1;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_category);

        GridView grid = findViewById(R.id.gridView);
        selectedColorView = findViewById(R.id.selectedColorView);
        categoryNameEditText = findViewById(R.id.categoryName);

        String categoryName = getIntent().getStringExtra("categoryName");
        int selectedColor = getIntent().getIntExtra("selectedColor", 0);

        categoryNameEditText.setText(categoryName);
        selectedColorView.setBackgroundColor(selectedColor);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        grid.setAdapter(new ExpenseColorAdapter(this,colors,selectedColor));

        Toolbar toolbar = findViewById(R.id.my_toolBar);
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

        Button btnDeleteCategory = findViewById(R.id.buttonDeleteCategory);
        btnDeleteCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCategoryFromFirestore();
            }
        });

        Button btnEditCategory = findViewById(R.id.buttonEditCategory);

        btnEditCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String categoryName = categoryNameEditText.getText().toString().trim();

                if (categoryName.isEmpty()) {
                    categoryNameEditText.setError("Category name is required");
                } else {
                    categoryNameEditText.setError(null);

                    if(selectedColorPosition!=-1){
                        saveEditedCategoryToFirestore(colors[selectedColorPosition]);
                    }
                    else{
                        saveEditedCategoryToFirestore(selectedColor);
                    }

                    startActivity(new Intent(editCategory.this, Homepage.class));
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
        private int[] colors;
        private int selectedColor;

        public ExpenseColorAdapter(Context c, int[] colors, int selectedColor) {
            context = c;
            this.colors = colors;
            this.selectedColor = selectedColor;
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

                if (selectedColor == colors[position]) {
                    colorView.setBackgroundColor(selectedColor);
                }
            } else {
                cardView = (CardView) convertView;
            }

            ImageView colorView = (ImageView) cardView.getChildAt(0);
            colorView.setBackgroundColor(colors[position]);

            return cardView;
        }
    }

    private void deleteCategoryFromFirestore() {
        String categoryId = getIntent().getStringExtra("categoryId");
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("expenses")
                .document(userId)
                .collection("user_categories")
                .document(categoryId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Category deleted with ID: " + categoryId);
                        startActivity(new Intent(editCategory.this, Homepage.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error deleting category", e);
                    }
                });
    }

    private void saveEditedCategoryToFirestore(int selectedColor) {
        String categoryId = getIntent().getStringExtra("categoryId");
        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> categoryData = new HashMap<>();

        categoryData.put("selectedColor", selectedColor);

        categoryData.put("categoryName", categoryNameEditText.getText().toString());

        db.collection("expenses")
                .document(userId)
                .collection("user_categories")
                .document(categoryId)
                .update(categoryData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Category updated with ID: " + categoryId);
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error updating category", e);
                });
    }
}
