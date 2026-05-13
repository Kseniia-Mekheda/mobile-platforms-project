package com.example.taskforge.ui.finances;

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
import com.example.taskforge.data.entities.FinanceRecord;
import com.example.taskforge.domain.repositories.TaskForgeRepository;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

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
        colors.add(requireContext().getColor(R.color.ui_chart_pastel_1));
        colors.add(requireContext().getColor(R.color.ui_chart_pastel_2));
        colors.add(requireContext().getColor(R.color.ui_chart_pastel_3));
        colors.add(requireContext().getColor(R.color.ui_chart_pastel_4));
        colors.add(requireContext().getColor(R.color.ui_chart_pastel_5));
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
        boolean isEdit = existingRecord != null;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_finance, null, false);
        final TextInputEditText titleBox = dialogView.findViewById(R.id.etFinanceTitle);
        final TextInputEditText amountBox = dialogView.findViewById(R.id.etFinanceAmount);
        final Spinner typeSpinner = dialogView.findViewById(R.id.spFinanceType);
        final Spinner categorySpinner = dialogView.findViewById(R.id.spFinanceCategory);
        final TextInputEditText descBox = dialogView.findViewById(R.id.etFinanceDescription);
        final View btnCancel = dialogView.findViewById(R.id.btnCancelDialog);
        final View btnSave = dialogView.findViewById(R.id.btnSaveDialog);

        String[] types = new String[]{"Expense", "Income"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_dark, types);
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark);
        typeSpinner.setAdapter(typeAdapter);

        String[] categories = new String[]{"Food", "Transport", "Rent", "Salary", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_dark, categories);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark);
        categorySpinner.setAdapter(categoryAdapter);

        if (isEdit) {
            titleBox.setText(existingRecord.title);
            amountBox.setText(String.valueOf(existingRecord.amount));
            descBox.setText(existingRecord.description);
            if ("INCOME".equals(existingRecord.record_type)) {
                typeSpinner.setSelection(1);
            } else {
                typeSpinner.setSelection(0);
            }
            
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(existingRecord.category)) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
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
            String title = titleBox.getText() != null ? titleBox.getText().toString().trim() : "";
            String amountStr = amountBox.getText() != null ? amountBox.getText().toString().trim() : "";
            String desc = descBox.getText() != null ? descBox.getText().toString().trim() : "";
            String category = categorySpinner.getSelectedItem().toString();
            String type = typeSpinner.getSelectedItemPosition() == 1 ? "INCOME" : "EXPENSE";

            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Назва і сума обов'язкові", Toast.LENGTH_SHORT).show();
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
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Некоректна сума", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDeleteDialog(FinanceRecord record) {
        new MaterialAlertDialogBuilder(requireContext())
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