package com.example.taskforge.ui.tasks;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.Task;
import com.example.taskforge.data.entities.User;
import com.example.taskforge.domain.repositories.TaskForgeRepository;
import com.example.taskforge.domain.utils.TaskSorter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class TasksActivity extends AppCompatActivity {

    private long projectId;
    private long currentUserId;
    private TaskForgeRepository repository;
    private RecyclerView rvTasks;
    private TaskAdapter adapter;
    private List<User> projectUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        projectId = getIntent().getLongExtra("PROJECT_ID", -1);

        SharedPreferences prefs = getSharedPreferences("TaskForgePrefs", MODE_PRIVATE);
        currentUserId = prefs.getLong("logged_in_user_id", -1);

        repository = new TaskForgeRepository(getApplication());

        if (projectId != -1) {
            projectUsers = repository.getUsersForProject(projectId);
        } else {
            projectUsers = new ArrayList<>();
        }

        rvTasks = findViewById(R.id.rvTasks);
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(new ArrayList<>(), task -> showTaskDialog(task));
        rvTasks.setAdapter(adapter);

        loadTasks();

        fabAddTask.setOnClickListener(v -> showTaskDialog(null));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks(); 
    }

    private void loadTasks() {
        if (projectId != -1) {
            List<Task> tasks = repository.getTasksForProject(projectId);
            if (tasks != null) {
                TaskSorter.sortByPriorityAndDate(tasks);
                runOnUiThread(() -> {
                        adapter.setTasks(tasks);
                    });
            }
        }
    }

    private void showTaskDialog(Task existingTask) {
        boolean isEdit = existingTask != null;
        android.view.View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_task, null, false);
        final TextInputEditText titleBox = dialogView.findViewById(R.id.etTaskTitle);
        final Spinner categorySpinner = dialogView.findViewById(R.id.spTaskCategory);
        final View btnCancel = dialogView.findViewById(R.id.btnCancelDialog);
        final View btnSave = dialogView.findViewById(R.id.btnSaveDialog);
        String[] categories = new String[]{"Development", "Design", "QA", "Marketing", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, categories);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark);
        categorySpinner.setAdapter(categoryAdapter);

        final Spinner prioritySpinner = dialogView.findViewById(R.id.spTaskPriority);
        String[] priorities = new String[]{"High", "Medium", "Low"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, priorities);
        priorityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark);
        prioritySpinner.setAdapter(priorityAdapter);

        final Spinner statusSpinner = dialogView.findViewById(R.id.spTaskStatus);
        String[] statuses = new String[]{"ToDo", "InProgress", "Done"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, statuses);
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark);
        statusSpinner.setAdapter(statusAdapter);

        final Spinner assigneeSpinner = dialogView.findViewById(R.id.spTaskAssignee);
        List<String> userNames = new ArrayList<>();
        for (User u : projectUsers) {
            userNames.add(u.name);
        }
        if (userNames.isEmpty()) userNames.add("Unassigned");
        ArrayAdapter<String> assigneeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, userNames);
        assigneeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark);
        assigneeSpinner.setAdapter(assigneeAdapter);

        if (isEdit) {
            titleBox.setText(existingTask.title);
            setSpinnerSelection(categorySpinner, categories, existingTask.category);
            setSpinnerSelection(prioritySpinner, priorities, existingTask.priority);
            setSpinnerSelection(statusSpinner, statuses, existingTask.status);
            
            if (existingTask.assignee_id != null) {
                for (int i = 0; i < projectUsers.size(); i++) {
                    if (projectUsers.get(i).id == existingTask.assignee_id) {
                        assigneeSpinner.setSelection(i);
                        break;
                    }
                }
            }
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String title = titleBox.getText() != null ? titleBox.getText().toString().trim() : "";
            String category = categorySpinner.getSelectedItem().toString();
            String priority = prioritySpinner.getSelectedItem().toString();
            String status = statusSpinner.getSelectedItem().toString();
            
            Long selectedAssigneeId = null;
            if (!projectUsers.isEmpty()) {
                int selectedPos = assigneeSpinner.getSelectedItemPosition();
                if (selectedPos >= 0 && selectedPos < projectUsers.size()) {
                    selectedAssigneeId = projectUsers.get(selectedPos).id;
                }
            }

            if (title.isEmpty()) {
                Toast.makeText(this, "Назва не може бути порожньою", Toast.LENGTH_SHORT).show();
            } else {
                if (isEdit) {
                    existingTask.title = title;
                    existingTask.category = category;
                    existingTask.priority = priority;
                    existingTask.status = status;
                    existingTask.assignee_id = selectedAssigneeId;
                    repository.updateTask(existingTask);
                } else {
                    long dueDate = System.currentTimeMillis() + 86400000; // +1 day
                    Task t = new Task(projectId, selectedAssigneeId, title, category, priority, status, dueDate, 0);
                    repository.insertTask(t);
                }
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadTasks();
                }, 300);
                dialog.dismiss();
            }
        });
    }

    private void setSpinnerSelection(Spinner spinner, String[] array, String value) {
        if (value == null) return;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}