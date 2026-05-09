package com.example.taskforge.ui.projects;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.Project;
import com.example.taskforge.data.entities.ProjectMember;
import com.example.taskforge.data.entities.User;
import com.example.taskforge.domain.repositories.TaskForgeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProjectActivity extends AppCompatActivity {

    private EditText etProjectName;
    private EditText etProjectDesc;
    private Button btnSaveProject;

    private EditText etUserEmail;
    private Button btnAddMember;

    private RecyclerView rvMembers;
    private ProjectMemberAdapter memberAdapter;

    private TaskForgeRepository repository;
    private ExecutorService executorService;

    private long projectId;
    private long loggedInUserId;
    private Project currentProject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_project);

        etProjectName = findViewById(R.id.etProjectName);
        etProjectDesc = findViewById(R.id.etProjectDesc);
        btnSaveProject = findViewById(R.id.btnSaveProject);

        etUserEmail = findViewById(R.id.etUserEmail);
        btnAddMember = findViewById(R.id.btnAddMember);

        rvMembers = findViewById(R.id.rvMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));

        repository = new TaskForgeRepository(getApplication());
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = getSharedPreferences("TaskForgePrefs", Context.MODE_PRIVATE);
        loggedInUserId = prefs.getLong("logged_in_user_id", -1);
        projectId = getIntent().getLongExtra("PROJECT_ID", -1);

        if (projectId == -1 || loggedInUserId == -1) {
            Toast.makeText(this, "Помилка завантаження проєкту", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        memberAdapter = new ProjectMemberAdapter(new ArrayList<>(), loggedInUserId, this::showKickConfirmationDialog);
        rvMembers.setAdapter(memberAdapter);

        btnSaveProject.setOnClickListener(v -> saveProject());
        btnAddMember.setOnClickListener(v -> addMemberByEmail());

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadProjectDetails();
                }, 300);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProjectDetails(); 
    }

    private void loadProjectDetails() {
        executorService.execute(() -> {
            currentProject = repository.getProjectById(projectId);
            if (currentProject != null) {
                runOnUiThread(() -> {
                    etProjectName.setText(currentProject.name);
                    etProjectDesc.setText(currentProject.description);
                });
            }
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadMembers();
                }, 300);
        });
    }

    private void loadMembers() {
        List<User> members = repository.getUsersForProject(projectId);
        if (members != null) {
            runOnUiThread(() -> memberAdapter.setMembers(members));
        }
    }

    private void saveProject() {
        String name = etProjectName.getText().toString().trim();
        String desc = etProjectDesc.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Назва проєкту не може бути порожньою", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            if (currentProject != null) {
                currentProject.name = name;
                currentProject.description = desc;
                repository.updateProject(currentProject);

                runOnUiThread(() -> Toast.makeText(this, "Проєкт збережено", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void addMemberByEmail() {
        String email = etUserEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Введіть email", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            User foundUser = repository.getUserByEmail(email);
            if (foundUser == null) {
                runOnUiThread(() -> Toast.makeText(this, "Користувача не знайдено", Toast.LENGTH_SHORT).show());
            } else {
                int count = repository.isUserInProject(projectId, foundUser.id);
                if (count > 0) {
                    runOnUiThread(() -> Toast.makeText(this, "Користувач вже у проєкті", Toast.LENGTH_SHORT).show());
                } else {
                    repository.insertProjectMember(new ProjectMember(projectId, foundUser.id));
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Користувача додано", Toast.LENGTH_SHORT).show();
                        etUserEmail.setText("");
                    });
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadMembers();
                }, 300);
                }
            }
        });
    }

    private void showKickConfirmationDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Видалити учасника")
                .setMessage("Ви впевнені, що хочете видалити " + user.name + "?")
                .setPositiveButton("Так", (dialog, which) -> kickMember(user.id))
                .setNegativeButton("Ні", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void kickMember(long userId) {
        executorService.execute(() -> {
            repository.removeProjectMember(projectId, userId);
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadMembers();
                }, 300);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}