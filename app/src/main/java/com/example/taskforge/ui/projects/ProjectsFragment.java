package com.example.taskforge.ui.projects;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.Project;
import com.example.taskforge.data.entities.ProjectMember;
import com.example.taskforge.domain.repositories.TaskForgeRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProjectsFragment extends Fragment {

    private RecyclerView rvProjects;
    private FloatingActionButton fabAddProject;
    
    private TaskForgeRepository repository;
    private long loggedInUserId;
    private ExecutorService executorService;
    private ProjectAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projects, container, false);

        rvProjects = view.findViewById(R.id.rvProjects);
        fabAddProject = view.findViewById(R.id.fabAddProject);

        rvProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        repository = new TaskForgeRepository(requireActivity().getApplication());
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = requireActivity().getSharedPreferences("TaskForgePrefs", Context.MODE_PRIVATE);
        loggedInUserId = prefs.getLong("logged_in_user_id", -1);

        adapter = new ProjectAdapter(new ArrayList<>(), loggedInUserId, project -> {
            Intent intent = new Intent(getActivity(), com.example.taskforge.ui.tasks.TasksActivity.class);
            intent.putExtra("PROJECT_ID", project.id);
            startActivity(intent);
        }, projectId -> {
            Intent intent = new Intent(getActivity(), EditProjectActivity.class);
            intent.putExtra("PROJECT_ID", projectId);
            startActivity(intent);
        });
        rvProjects.setAdapter(adapter);

        fabAddProject.setOnClickListener(v -> showAddProjectDialog());

        loadProjects();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProjects(); 
    }

    private void loadProjects() {
        executorService.execute(() -> {
            List<Project> projects = repository.getProjectsForUser(loggedInUserId);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (projects != null) {
                        adapter.setProjects(projects);
                    }
                });
            }
        });
    }

    private void showAddProjectDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_project, null, false);
        final TextInputEditText titleBox = dialogView.findViewById(R.id.etProjectTitle);
        final TextInputEditText descBox = dialogView.findViewById(R.id.etProjectDescription);
        View btnCancel = dialogView.findViewById(R.id.btnCancelDialog);
        View btnSave = dialogView.findViewById(R.id.btnSaveDialog);

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
            String desc = descBox.getText() != null ? descBox.getText().toString().trim() : "";

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            executorService.execute(() -> {
                Project p = new Project(loggedInUserId, title, desc);
                long createdProjectId = repository.insertProject(p);
                if (createdProjectId != -1) {
                    repository.insertProjectMember(new ProjectMember(createdProjectId, loggedInUserId));
                    loadProjects();
                }
            });
            dialog.dismiss();
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