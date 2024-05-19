package com.example.myexpensetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class OverviewListAdapter extends RecyclerView.Adapter<OverviewListAdapter.ExpenseViewHolder> {

    private List<MonthlyItem> monthlyItemList;
    private Context context;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(String monthName);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public OverviewListAdapter(Context context, List<MonthlyItem> monthlyItemList) {
        this.context = context;
        this.monthlyItemList = monthlyItemList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        MonthlyItem expenseMonthItem = monthlyItemList.get(position);
        holder.bind(expenseMonthItem);

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(expenseMonthItem.getMonthYear());
            }
        });
    }

    @Override
    public int getItemCount() {
        return monthlyItemList.size();
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView monthTextView;
        TextView expenseAmountTextView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            monthTextView = itemView.findViewById(R.id.monthTextView);
            expenseAmountTextView = itemView.findViewById(R.id.expenseAmountTextView);
        }

        public void bind(MonthlyItem expenseMonthItem) {
            monthTextView.setText(expenseMonthItem.getMonthYear());
            expenseAmountTextView.setText(String.format(Locale.getDefault(), "RM %.2f", expenseMonthItem.getExpenseAmount()));
        }
    }
}
