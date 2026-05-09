package com.example.taskforge.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.User;
import com.example.taskforge.domain.repositories.TaskForgeRepository;
import com.example.taskforge.ui.auth.LoginActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    private EditText etProfileName;
    private EditText etProfileEmail;
    private Button btnSaveProfile;
    private Button btnLogout;

    private TaskForgeRepository repository;
    private ExecutorService executorService;
    private long loggedInUserId;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        etProfileName = view.findViewById(R.id.etProfileName);
        etProfileEmail = view.findViewById(R.id.etProfileEmail);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        repository = new TaskForgeRepository(requireActivity().getApplication());
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = requireActivity().getSharedPreferences("TaskForgePrefs", Context.MODE_PRIVATE);
        loggedInUserId = prefs.getLong("logged_in_user_id", -1);

        if (loggedInUserId == -1) {
            logout();
            return view;
        }

        loadUserData();

        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData(); 
    }

    private void loadUserData() {
        executorService.execute(() -> {
            currentUser = repository.getUserById(loggedInUserId);
            if (currentUser != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    etProfileName.setText(currentUser.name);
                    etProfileEmail.setText(currentUser.email);
                });
            }
        });
    }

    private void saveProfile() {
        String newName = etProfileName.getText().toString().trim();
        String newEmail = etProfileEmail.getText().toString().trim();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(getContext(), "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            executorService.execute(() -> {
                currentUser.name = newName;
                currentUser.email = newEmail;
                repository.updateUser(currentUser);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                            Toast.makeText(getContext(), "Дані оновлено", Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }
    }

    private void showLogoutConfirmation() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Вихід")
                .setMessage("Ви дійсно хочете вийти?")
                .setPositiveButton("Так", (dialog, which) -> logout())
                .setNegativeButton("Ні", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logout() {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences("TaskForgePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("logged_in_user_id", -1);
        editor.apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}