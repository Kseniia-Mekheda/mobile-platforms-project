package com.example.taskforge.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.taskforge.R;
import com.example.taskforge.ui.auth.LoginActivity;
import com.example.taskforge.ui.finances.FinancesFragment;
import com.example.taskforge.ui.profile.ProfileFragment;
import com.example.taskforge.ui.projects.ProjectsFragment;
import com.example.taskforge.ui.subscriptions.SubscriptionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Перевірка авторизації
        SharedPreferences prefs = getSharedPreferences("TaskForgePrefs", MODE_PRIVATE);
        long userId = prefs.getLong("logged_in_user_id", -1);

        if (userId == -1) {
            // Якщо не авторизовані - переходимо на Login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            
            if (id == R.id.nav_projects) {
                selectedFragment = new ProjectsFragment();
            } else if (id == R.id.nav_finances) {
                selectedFragment = new FinancesFragment();
            } else if (id == R.id.nav_subscriptions) {
                selectedFragment = new SubscriptionsFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Завантажуємо перший фрагмент за замовчуванням (Projects)
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_projects);
        }
    }
}
