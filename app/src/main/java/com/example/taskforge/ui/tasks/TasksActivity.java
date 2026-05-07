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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        projectId = getIntent().getLongExtra("PROJECT_ID", -1);
        String projectName = getIntent().getStringExtra("PROJECT_NAME");

        if (projectName != null) {
            setTitle("Завдання: " + projectName);
        }

        SharedPreferences prefs = getSharedPreferences("TaskForgePrefs", MODE_PRIVATE);
        currentUserId = prefs.getLong("logged_in_user_id", -1);

        repository = new TaskForgeRepository(getApplication());

        rvTasks = findViewById(R.id.rvTasks);
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(new ArrayList<>());
        rvTasks.setAdapter(adapter);

        loadTasks();

        fabAddTask.setOnClickListener(v -> showAddTaskDialog());
    }

    private void loadTasks() {
        List<Task> tasks = repository.getTasksForProject(projectId);
        if (tasks != null) {
            TaskSorter.sortByPriorityAndDate(tasks);
            adapter.setTasks(tasks);
        }
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Нове завдання");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText titleBox = new EditText(this);
        titleBox.setHint("Назва завдання");
        layout.addView(titleBox);

        final Spinner prioritySpinner = new Spinner(this);
        String[] priorities = new String[]{"High", "Med", "Low"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        prioritySpinner.setAdapter(priorityAdapter);
        layout.addView(prioritySpinner);

        final Spinner statusSpinner = new Spinner(this);
        String[] statuses = new String[]{"ToDo", "InProgress", "Done"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        statusSpinner.setAdapter(statusAdapter);
        layout.addView(statusSpinner);

        builder.setView(layout);

        builder.setPositiveButton("Додати", (dialog, which) -> {
            String title = titleBox.getText().toString().trim();
            String priority = prioritySpinner.getSelectedItem().toString();
            String status = statusSpinner.getSelectedItem().toString();

            if (title.isEmpty()) {
                Toast.makeText(this, "Назва не може бути порожньою", Toast.LENGTH_SHORT).show();
            } else {
                // В якості due_date тимчасово ставимо поточний час + 1 день
                long dueDate = System.currentTimeMillis() + 86400000;
                Task t = new Task(projectId, currentUserId, title, "General", priority, status, dueDate, 0);
                repository.insertTask(t);
                loadTasks();
            }
        });
        builder.setNegativeButton("Скасувати", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
