package com.example.myexpensetracker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private Context context;
    private OnExpenseItemClickListener eListener;

    public ExpenseAdapter(Context context, List<Expense> expenseList) {
        this.context = context;
        this.expenseList = expenseList;
    }

    public interface OnExpenseItemClickListener {
        void onItemClick(Expense expense);
    }

    public void setOnExpenseItemClickListener(OnExpenseItemClickListener listener) {
        eListener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_list_item, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        Expense currentExpense = expenseList.get(position);
        holder.amountTextView.setText(String.valueOf(currentExpense.getAmount()));
        holder.noteTextView.setText(currentExpense.getNote());
        holder.dateTextView.setText(String.valueOf(currentExpense.getDate()));

        if (expense != null) {
            holder.bind(expense);

            holder.itemView.setOnClickListener(v -> {
                if (eListener != null) {
                    eListener.onItemClick(expense);
                    Log.d("ExpenseDetailsAdapter", "Clicked Expense ID: " + currentExpense.getExpenseId());
                }
            });
            Log.d("ExpenseDetailsAdapter", "Expense List Size: " + expenseList.size());
            Log.d("ExpenseDetailsAdapter", "Expense at position " + position + ": " + expense.toString());
        } else {
            Log.e("ExpenseDetailsAdapter", "Expense at position " + position + " is null");
        }
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder{
        TextView noteTextView;
        TextView amountTextView;
        TextView dateTextView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(Expense expense) {
            noteTextView.setText(expense.getNote());
            amountTextView.setText(String.format(Locale.getDefault(), "RM %.2f", expense.getAmount()));
            dateTextView.setText(getFormattedDate(expense.getDate()));

            Log.d("ExpenseViewHolder", "Note: " + expense.getNote());
            Log.d("ExpenseViewHolder", "Amount: " + String.format(Locale.getDefault(), "RM %.2f", expense.getAmount()));
            Log.d("ExpenseViewHolder", "Date: " + getFormattedDate(expense.getDate()));
        }

        private String getFormattedDate(Timestamp timestamp) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            Date date = new Date(timestamp.getSeconds() * 1000); // Convert to milliseconds
            return dateFormat.format(date);
        }
    }
}
