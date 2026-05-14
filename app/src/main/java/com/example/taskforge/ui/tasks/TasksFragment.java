package com.example.taskforge.ui.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.Project;
import com.example.taskforge.data.entities.Task;
import com.example.taskforge.domain.repositories.TaskForgeRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TasksFragment extends Fragment {

    private RecyclerView rvTasks;
    private FloatingActionButton fabAddTask;
    private TaskAdapter taskAdapter;
    private TaskForgeRepository repository;
    private long loggedInUserId;
    private ExecutorService executorService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        rvTasks = view.findViewById(R.id.rvTasks);
        fabAddTask = view.findViewById(R.id.fabAddTask);

        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        repository = new TaskForgeRepository(requireActivity().getApplication());
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = requireActivity().getSharedPreferences("TaskForgePrefs", Context.MODE_PRIVATE);
        loggedInUserId = prefs.getLong("logged_in_user_id", -1);

        fabAddTask.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Add task", Toast.LENGTH_SHORT).show();
        });

        loadTasks();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks(); 
    }

    private void loadTasks() {
        executorService.execute(() -> {
            List<Task> allTasks = new ArrayList<>();
            List<Project> projects = repository.getProjectsForUser(loggedInUserId);
            
            if (projects != null) {
                for (Project project : projects) {
                    List<Task> tasksForProject = repository.getTasksForProject(project.id);
                    if (tasksForProject != null) {
                        allTasks.addAll(tasksForProject);
                    }
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    taskAdapter = new TaskAdapter(allTasks, null);
                    rvTasks.setAdapter(taskAdapter);
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}