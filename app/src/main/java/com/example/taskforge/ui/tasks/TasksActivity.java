package com.example.taskforge.ui.tasks;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.Task;
import com.example.taskforge.data.entities.User;
import com.example.taskforge.domain.repositories.TaskForgeRepository;
import com.example.taskforge.domain.utils.TaskSorter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        boolean isEdit = existingTask != null;
        builder.setTitle(isEdit ? "Edit Task" : "New Task");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText titleBox = new EditText(this);
        titleBox.setHint("Task Name");
        layout.addView(titleBox);

        final Spinner categorySpinner = new Spinner(this);
        String[] categories = new String[]{"Development", "Design", "QA", "Marketing", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(categoryAdapter);
        layout.addView(categorySpinner);

        final Spinner prioritySpinner = new Spinner(this);
        String[] priorities = new String[]{"High", "Medium", "Low"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        prioritySpinner.setAdapter(priorityAdapter);
        layout.addView(prioritySpinner);

        final Spinner statusSpinner = new Spinner(this);
        String[] statuses = new String[]{"ToDo", "InProgress", "Done"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        statusSpinner.setAdapter(statusAdapter);
        layout.addView(statusSpinner);

        final Spinner assigneeSpinner = new Spinner(this);
        List<String> userNames = new ArrayList<>();
        for (User u : projectUsers) {
            userNames.add(u.name);
        }
        if (userNames.isEmpty()) userNames.add("Unassigned");
        ArrayAdapter<String> assigneeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, userNames);
        assigneeSpinner.setAdapter(assigneeAdapter);
        layout.addView(assigneeSpinner);

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

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = titleBox.getText().toString().trim();
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
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
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
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
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