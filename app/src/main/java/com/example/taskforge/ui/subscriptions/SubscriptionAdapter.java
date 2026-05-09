package com.example.taskforge.ui.subscriptions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskforge.R;
import com.example.taskforge.data.entities.Subscription;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.ViewHolder> {

    private List<Subscription> subscriptions;
    private OnSubClickListener clickListener;
    private OnSubLongClickListener longClickListener;
    private OnReminderToggleListener reminderListener;

    public interface OnSubClickListener { void onClick(Subscription sub); }
    public interface OnSubLongClickListener { void onLongClick(Subscription sub); }
    public interface OnReminderToggleListener { void onToggle(Subscription sub, boolean isChecked); }

    public SubscriptionAdapter(List<Subscription> subscriptions, OnSubClickListener click, OnSubLongClickListener longClick, OnReminderToggleListener reminder) {
        this.subscriptions = subscriptions;
        this.clickListener = click;
        this.longClickListener = longClick;
        this.reminderListener = reminder;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subscription sub = subscriptions.get(position);
        holder.tvTitle.setText(sub.title);
        holder.tvCategory.setText(sub.category);

        // ВИПРАВЛЕНО: amount замість price
        holder.tvPrice.setText("-" + sub.amount + " " + sub.currency);

        // ВИПРАВЛЕНО: repeat_interval замість billing_cycle_ms
        long nextBillingMs = sub.start_date_ms + sub.repeat_interval;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        holder.tvNextBilling.setText("Наступна оплата: " + sdf.format(new Date(nextBillingMs)));

        holder.switchReminder.setOnCheckedChangeListener(null);
        holder.switchReminder.setChecked(sub.reminder_enabled);

        holder.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminderListener.onToggle(sub, isChecked);
        });

        holder.itemView.setOnClickListener(v -> clickListener.onClick(sub));
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onLongClick(sub);
            return true;
        });
    }

    @Override
    public int getItemCount() { return subscriptions != null ? subscriptions.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvPrice, tvNextBilling;
        SwitchCompat switchReminder;
        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvSubTitle);
            tvCategory = view.findViewById(R.id.tvSubCategory);
            tvPrice = view.findViewById(R.id.tvSubPrice);
            tvNextBilling = view.findViewById(R.id.tvNextBilling);
            switchReminder = view.findViewById(R.id.switchReminder);
        }
    }
}