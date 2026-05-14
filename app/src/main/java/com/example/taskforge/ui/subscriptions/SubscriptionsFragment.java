package com.example.taskforge.ui.subscriptions;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.TaskForgeDatabase;
import com.example.taskforge.data.entities.Subscription;
import com.example.taskforge.domain.repositories.TaskForgeRepository;
import com.example.taskforge.domain.utils.AlarmScheduler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionsFragment extends Fragment {

    private RecyclerView rvSubscriptions;
    private SubscriptionAdapter adapter;
    private TaskForgeRepository repository;
    private long loggedInUserId = -1;

    // Константи для інтервалів у мілісекундах
    private final long MINUTE_MS = 60000L;
    private final long WEEK_MS = 604800000L;
    private final long MONTH_MS = 2592000000L;
    private final long YEAR_MS = 31536000000L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscriptions, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences("TaskForgePrefs", Context.MODE_PRIVATE);
        loggedInUserId = prefs.getLong("logged_in_user_id", -1);
        repository = new TaskForgeRepository(requireActivity().getApplication());

        rvSubscriptions = view.findViewById(R.id.rvSubscriptions);
        rvSubscriptions.setLayoutManager(new LinearLayoutManager(getContext()));
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddSubscription);

        adapter = new SubscriptionAdapter(new ArrayList<>(),
                this::showSubDialog,
                this::deleteSub,
                this::toggleReminder
        );
        rvSubscriptions.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showSubDialog(null));

        loadData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(); 
    }

    private void loadData() {
        TaskForgeDatabase.databaseWriteExecutor.execute(() -> {
            List<Subscription> subs = repository.getSubscriptionsForUser(loggedInUserId);
            requireActivity().runOnUiThread(() -> adapter.setSubscriptions(subs));
        });
    }

    private void showSubDialog(@Nullable Subscription existingSub) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_subscription, null, false);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etSubscriptionTitle);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etSubscriptionAmount);
        Spinner spCategory = dialogView.findViewById(R.id.spSubscriptionCategory);
        View btnCancel = dialogView.findViewById(R.id.btnCancelDialog);
        View btnSave = dialogView.findViewById(R.id.btnSaveDialog);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_dark, new String[]{"Entertainment", "Software", "Other"});
        catAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark);
        spCategory.setAdapter(catAdapter);

        Spinner spInterval = dialogView.findViewById(R.id.spSubscriptionInterval);
        String[] intervalNames = {"Minute (Test)", "Weekly", "Monthly", "Yearly"};
        long[] intervalValues = {MINUTE_MS, WEEK_MS, MONTH_MS, YEAR_MS};
        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_dark, intervalNames);
        intervalAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark);
        spInterval.setAdapter(intervalAdapter);

        if (existingSub != null) {
            etTitle.setText(existingSub.title);
            etPrice.setText(String.valueOf(existingSub.amount));

            // Встановлюємо правильний інтервал при редагуванні
            if (existingSub.repeat_interval == MINUTE_MS) spInterval.setSelection(0);
            else if (existingSub.repeat_interval == WEEK_MS) spInterval.setSelection(1);
            else if (existingSub.repeat_interval == YEAR_MS) spInterval.setSelection(3);
            else spInterval.setSelection(2); // за замовчуванням місяць
        } else {
            spInterval.setSelection(2); // За замовчуванням вибираємо "Місячна"
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String amountValue = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
            String cat = spCategory.getSelectedItem().toString();
            long selectedInterval = intervalValues[spInterval.getSelectedItemPosition()];
            double amount;

            try {
                amount = Double.parseDouble(amountValue.isEmpty() ? "0" : amountValue);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Incorrect amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingSub == null) {
                Subscription newSub = new Subscription(loggedInUserId, title, "", cat, amount, "UAH", System.currentTimeMillis(), selectedInterval, false);
                TaskForgeDatabase.databaseWriteExecutor.execute(() -> {
                    repository.insertSubscription(newSub);
                    loadData();
                });
                dialog.dismiss();
            } else {
                existingSub.title = title;
                existingSub.amount = amount;
                existingSub.category = cat;
                existingSub.repeat_interval = selectedInterval;

                TaskForgeDatabase.databaseWriteExecutor.execute(() -> {
                    repository.updateSubscription(existingSub);
                    if(existingSub.reminder_enabled) {
                        // Перезапускаємо будильник з новим інтервалом
                        AlarmScheduler.scheduleSubscriptionReminder(requireContext(), existingSub);
                    }
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadData();
                }, 300);
                });
                dialog.dismiss();
            }
        });
    }

    private void deleteSub(Subscription sub) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete the subscription?")
                .setPositiveButton("Yes", (d, w) -> {
                    AlarmScheduler.cancelSubscriptionReminder(requireContext(), sub.id);
                    TaskForgeDatabase.databaseWriteExecutor.execute(() -> {
                        repository.deleteSubscription(sub);
                        loadData();
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void toggleReminder(Subscription sub, boolean isChecked) {
        sub.reminder_enabled = isChecked;
        TaskForgeDatabase.databaseWriteExecutor.execute(() -> {
            repository.updateSubscription(sub);
            requireActivity().runOnUiThread(() -> {
                if (isChecked) {
                    AlarmScheduler.scheduleSubscriptionReminder(requireContext(), sub);
                    Toast.makeText(getContext(), "Timer is on (60 sec)!", Toast.LENGTH_SHORT).show();
                } else {
                    AlarmScheduler.cancelSubscriptionReminder(requireContext(), sub.id);
                    Toast.makeText(getContext(), "Notification done", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}