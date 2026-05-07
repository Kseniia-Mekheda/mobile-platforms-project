package com.example.taskforge.ui.projects;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.Project;
import com.example.taskforge.domain.repositories.TaskForgeRepository;
import com.example.taskforge.ui.tasks.TasksActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ProjectsFragment extends Fragment {

    private RecyclerView rvProjects;
    private ProjectAdapter adapter;
    private TaskForgeRepository repository;
    private long currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projects, container, false);

        rvProjects = view.findViewById(R.id.rvProjects);
        FloatingActionButton fabAddProject = view.findViewById(R.id.fabAddProject);

        rvProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProjectAdapter(new ArrayList<>(), project -> {
            Intent intent = new Intent(getActivity(), TasksActivity.class);
            intent.putExtra("PROJECT_ID", project.id);
            intent.putExtra("PROJECT_NAME", project.name);
            startActivity(intent);
        });
        rvProjects.setAdapter(adapter);

        repository = new TaskForgeRepository(getActivity().getApplication());

        SharedPreferences prefs = getActivity().getSharedPreferences("TaskForgePrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("logged_in_user_id", -1);

        loadProjects();

        fabAddProject.setOnClickListener(v -> showAddProjectDialog());

        return view;
    }

    private void loadProjects() {
        if (currentUserId != -1) {
            List<Project> projects = repository.getProjectsForUser(currentUserId);
            if (projects != null) {
                adapter.setProjects(projects);
            }
        }
    }

    private void showAddProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Новий Проєкт");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText titleBox = new EditText(getContext());
        titleBox.setHint("Назва проєкту");
        layout.addView(titleBox);

        final EditText descBox = new EditText(getContext());
        descBox.setHint("Опис");
        layout.addView(descBox);

        builder.setView(layout);

        builder.setPositiveButton("Створити", (dialog, which) -> {
            String title = titleBox.getText().toString().trim();
            String desc = descBox.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Назва не може бути порожньою", Toast.LENGTH_SHORT).show();
            } else {
                Project p = new Project(currentUserId, title, desc);
                repository.insertProject(p);
                loadProjects(); // Reload list
            }
        });
        builder.setNegativeButton("Скасувати", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
