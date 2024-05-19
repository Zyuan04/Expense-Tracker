package com.example.myexpensetracker;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CategoryAdapter extends FirestoreRecyclerAdapter<Category, CategoryAdapter.CategoryViewHolder> {
    private OnCategoryClickListener cListener;
    private FirebaseFirestore db;
    private FirebaseAuth fAuth;
    private String userId;
    private List<Category> categoryList;

    public void clearData() {
        if (categoryList != null) {
            categoryList.clear();
            notifyDataSetChanged();
        }
    }

    public CategoryAdapter(@NonNull FirestoreRecyclerOptions<Category> options) {
        super(options);
        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
    }

    public void updateOptions(FirestoreRecyclerOptions<Category> options) {
        new Handler(Looper.getMainLooper()).post(() -> {
            getSnapshots().clear();

            super.updateOptions(options);
        });
    }

    @Override
    protected void onBindViewHolder(@NonNull CategoryViewHolder holder, int position, @NonNull Category model) {
        if (position != RecyclerView.NO_POSITION) {
            int selectedColor = model.getSelectedColor();
            holder.categoryImage.setBackgroundColor(selectedColor);
            holder.categoryName.setText(model.getCategoryName());

            DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);
            String categoryId = documentSnapshot.getId();

            setExpenseAmountText(holder, categoryId);
        }
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    int selectedColor = model.getSelectedColor();
                    holder.categoryImage.setBackgroundColor(selectedColor);
                    holder.categoryName.setText(model.getCategoryName());

                    DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);
                    String categoryId = documentSnapshot.getId();
                    Category category = getItem(position);

                    if (category != null) {
                        Intent intent = new Intent(holder.itemView.getContext(), editCategory.class);
                        intent.putExtra("categoryName", model.getCategoryName());
                        intent.putExtra("selectedColor", selectedColor);
                        intent.putExtra("categoryId", categoryId);
                        holder.itemView.getContext().startActivity(intent);
                    }
                }
            }
        });
    }

    private void setExpenseAmountText(@NonNull CategoryViewHolder holder, String categoryId) {
        userId = fAuth.getCurrentUser().getUid();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timestamp startOfMonth = new Timestamp(calendar.getTime());

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        Timestamp endOfMonth = new Timestamp(calendar.getTime());

        CollectionReference userExpensesRef = db.collection("expenses").document(userId)
                .collection("user_categories")
                .document(categoryId)
                .collection("user_expenses");

        Query expenseQuery = userExpensesRef
                .whereGreaterThanOrEqualTo("date", startOfMonth)
                .whereLessThanOrEqualTo("date", endOfMonth);

        expenseQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                double categoryTotalAmount = 0.0;

                for (QueryDocumentSnapshot expenseDocument : task.getResult()) {
                    Expense expense = expenseDocument.toObject(Expense.class);
                    Log.d("Firestore", "Retrieved expense amount: " + expense.getAmount());
                    categoryTotalAmount += expense.getAmount();
                }

                if (holder.expenseAmountText != null) {
                    holder.expenseAmountText.setText(String.format(Locale.getDefault(), "RM %.2f", categoryTotalAmount));
                }
            } else {
                Log.e("Firestore", "Error getting user expenses for category " + categoryId, task.getException());
            }
        });
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        View categoryImage;
        TextView categoryName;
        TextView expenseAmountText;
        Button editButton;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryImage = itemView.findViewById(R.id.categoryImage);
            categoryName = itemView.findViewById(R.id.categoryName);
            expenseAmountText = itemView.findViewById(R.id.expenseAmountText);
            editButton = itemView.findViewById(R.id.editCategoryButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && cListener != null) {
                        cListener.onCategoryClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener cListener) {
        this.cListener = cListener;
    }
}