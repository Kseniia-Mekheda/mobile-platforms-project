package com.example.taskforge.ui.finances;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.FinanceRecord;
import com.example.taskforge.domain.repositories.TaskForgeRepository;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinancesFragment extends Fragment {

    private RecyclerView rvFinances;
    private FloatingActionButton fabAddFinance;
    private PieChart pieChart;

    private TaskForgeRepository repository;
    private long loggedInUserId;
    private ExecutorService executorService;
    private FinanceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finances, container, false);

        rvFinances = view.findViewById(R.id.rvFinances);
        fabAddFinance = view.findViewById(R.id.fabAddFinance);
        pieChart = view.findViewById(R.id.pieChart);

        setupPieChart();

        rvFinances.setLayoutManager(new LinearLayoutManager(getContext()));
        repository = new TaskForgeRepository(requireActivity().getApplication());
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = requireActivity().getSharedPreferences("TaskForgePrefs", Context.MODE_PRIVATE);
        loggedInUserId = prefs.getLong("logged_in_user_id", -1);

        adapter = new FinanceAdapter(new ArrayList<>(), this::showFinanceDialog, this::showDeleteDialog);
        rvFinances.setAdapter(adapter);

        fabAddFinance.setOnClickListener(v -> showFinanceDialog(null));

        loadFinances();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFinances(); 
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Витрати");
        pieChart.getLegend().setEnabled(false);
    }

    private void loadFinances() {
        executorService.execute(() -> {
            List<FinanceRecord> records = repository.getFinanceRecordsForUser(loggedInUserId);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (records != null) {
                        adapter.setRecords(records);
                        updateChart(records);
                    }
                });
            }
        });
    }

    private void updateChart(List<FinanceRecord> records) {
        Map<String, Float> expensesByCategory = new HashMap<>();

        for (FinanceRecord record : records) {
            if ("EXPENSE".equals(record.record_type)) {
                String category = record.category != null ? record.category : "Other";
                float currentSum = expensesByCategory.getOrDefault(category, 0f);
                expensesByCategory.put(category, currentSum + (float) record.amount);
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : expensesByCategory.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Витрати");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) colors.add(c);
        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        pieChart.setData(data);
        pieChart.highlightValues(null);
        pieChart.animateY(1400, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    private void showFinanceDialog(FinanceRecord existingRecord) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        boolean isEdit = existingRecord != null;
        builder.setTitle(isEdit ? "Edit Record" : "New Record");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText titleBox = new EditText(getContext());
        titleBox.setHint("Title");
        layout.addView(titleBox);

        final EditText amountBox = new EditText(getContext());
        amountBox.setHint("Amount");
        amountBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(amountBox);

        final RadioGroup typeGroup = new RadioGroup(getContext());
        typeGroup.setOrientation(LinearLayout.HORIZONTAL);
        RadioButton rbExpense = new RadioButton(getContext());
        rbExpense.setText("Expense");
        RadioButton rbIncome = new RadioButton(getContext());
        rbIncome.setText("Income");
        typeGroup.addView(rbExpense);
        typeGroup.addView(rbIncome);
        rbExpense.setChecked(true);
        layout.addView(typeGroup);

        final Spinner categorySpinner = new Spinner(getContext());
        String[] categories = new String[]{"Food", "Transport", "Rent", "Salary", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(categoryAdapter);
        layout.addView(categorySpinner);
        
        final EditText descBox = new EditText(getContext());
        descBox.setHint("Description");
        layout.addView(descBox);

        if (isEdit) {
            titleBox.setText(existingRecord.title);
            amountBox.setText(String.valueOf(existingRecord.amount));
            descBox.setText(existingRecord.description);
            if ("INCOME".equals(existingRecord.record_type)) {
                rbIncome.setChecked(true);
            } else {
                rbExpense.setChecked(true);
            }
            
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(existingRecord.category)) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
        }

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = titleBox.getText().toString().trim();
            String amountStr = amountBox.getText().toString().trim();
            String desc = descBox.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            String type = rbIncome.isChecked() ? "INCOME" : "EXPENSE";

            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Title and Amount are required", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    double amount = Double.parseDouble(amountStr);
                    executorService.execute(() -> {
                        if (isEdit) {
                            existingRecord.title = title;
                            existingRecord.amount = amount;
                            existingRecord.description = desc;
                            existingRecord.category = category;
                            existingRecord.record_type = type;
                            repository.updateFinanceRecord(existingRecord);
                        } else {
                            long dateMs = System.currentTimeMillis();
                            FinanceRecord record = new FinanceRecord(loggedInUserId, title, desc, type, category, amount, "UAH", dateMs);
                            repository.insertFinanceRecord(record);
                        }
                        loadFinances();
                    });
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showDeleteDialog(FinanceRecord record) {
        new AlertDialog.Builder(getContext())
            .setTitle("Видалити запис?")
            .setPositiveButton("Так", (dialog, which) -> {
                executorService.execute(() -> {
                    repository.deleteFinanceRecord(record);
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadFinances();
                }, 200);
                });
            })
            .setNegativeButton("Ні", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}