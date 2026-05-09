package com.example.taskforge.ui.subscriptions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.TaskForgeDatabase;
import com.example.taskforge.data.entities.Subscription;
import com.example.taskforge.domain.repositories.TaskForgeRepository;
import com.example.taskforge.domain.utils.AlarmScheduler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import com.example.taskforge.receivers.SubscriptionReceiver;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(existingSub == null ? "Нова підписка" : "Редагувати підписку");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etTitle = new EditText(getContext());
        etTitle.setHint("Назва (напр., Netflix)");
        layout.addView(etTitle);

        EditText etPrice = new EditText(getContext());
        etPrice.setHint("Сума");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etPrice);

        // Spinner для категорії
        Spinner spCategory = new Spinner(getContext());
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Розваги", "Софт", "Інше"});
        spCategory.setAdapter(catAdapter);
        layout.addView(spCategory);

        // НОВИЙ Spinner для періодичності
        Spinner spInterval = new Spinner(getContext());
        String[] intervalNames = {"Хвилинна (Тест)", "Тижнева", "Місячна", "Річна"};
        long[] intervalValues = {MINUTE_MS, WEEK_MS, MONTH_MS, YEAR_MS};
        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, intervalNames);
        spInterval.setAdapter(intervalAdapter);
        layout.addView(spInterval);

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

        builder.setView(layout);
        builder.setPositiveButton("Зберегти", (dialog, which) -> {
            String title = etTitle.getText().toString();
            double amount = Double.parseDouble(etPrice.getText().toString().isEmpty() ? "0" : etPrice.getText().toString());
            String cat = spCategory.getSelectedItem().toString();
            long selectedInterval = intervalValues[spInterval.getSelectedItemPosition()];

            if (existingSub == null) {
                Subscription newSub = new Subscription(loggedInUserId, title, "", cat, amount, "UAH", System.currentTimeMillis(), selectedInterval, false);
                TaskForgeDatabase.databaseWriteExecutor.execute(() -> {
                    repository.insertSubscription(newSub);
                    loadData();
                });
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
                    loadData();
                });
            }
        });
        builder.setNegativeButton("Скасувати", null);
        builder.show();
    }

    private void deleteSub(Subscription sub) {
        new AlertDialog.Builder(getContext())
                .setTitle("Видалити підписку?")
                .setPositiveButton("Так", (d, w) -> {
                    AlarmScheduler.cancelSubscriptionReminder(requireContext(), sub.id);
                    TaskForgeDatabase.databaseWriteExecutor.execute(() -> {
                        repository.deleteSubscription(sub);
                        loadData();
                    });
                })
                .setNegativeButton("Ні", null)
                .show();
    }

    private void toggleReminder(Subscription sub, boolean isChecked) {
        sub.reminder_enabled = isChecked;
        TaskForgeDatabase.databaseWriteExecutor.execute(() -> {
            repository.updateSubscription(sub);
            requireActivity().runOnUiThread(() -> {
                if (isChecked) {
                    AlarmScheduler.scheduleSubscriptionReminder(requireContext(), sub);
                    Toast.makeText(getContext(), "Таймер пішов (60 сек)!", Toast.LENGTH_SHORT).show();
                } else {
                    AlarmScheduler.cancelSubscriptionReminder(requireContext(), sub.id);
                    Toast.makeText(getContext(), "Нагадування вимкнено", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}