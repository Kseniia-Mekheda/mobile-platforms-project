package com.example.taskforge.ui.finances;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.FinanceRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinanceAdapter extends RecyclerView.Adapter<FinanceAdapter.FinanceViewHolder> {

    public interface OnFinanceClickListener {
        void onFinanceClick(FinanceRecord record);
    }

    public interface OnFinanceLongClickListener {
        void onFinanceLongClick(FinanceRecord record);
    }

    private List<FinanceRecord> financeList;
    private OnFinanceClickListener clickListener;
    private OnFinanceLongClickListener longClickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public FinanceAdapter(List<FinanceRecord> financeList, OnFinanceClickListener clickListener, OnFinanceLongClickListener longClickListener) {
        this.financeList = financeList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public FinanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_finance, parent, false);
        return new FinanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FinanceViewHolder holder, int position) {
        FinanceRecord record = financeList.get(position);
        holder.tvTitle.setText(record.title);
        holder.tvCategory.setText(record.category);
        holder.tvDate.setText(dateFormat.format(new Date(record.date_ms)));

        String amountString = String.format(Locale.getDefault(), "%.2f %s", record.amount, record.currency != null ? record.currency : "UAH");
        
        if ("INCOME".equals(record.record_type)) {
            holder.tvAmount.setText("+" + amountString);
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvAmount.setText("-" + amountString);
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")); // Red
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onFinanceClick(record);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onFinanceLongClick(record);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return financeList.size();
    }

    public void setRecords(List<FinanceRecord> records) {
        this.financeList = records;
        notifyDataSetChanged();
    }

    static class FinanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvAmount, tvDate;

        public FinanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}