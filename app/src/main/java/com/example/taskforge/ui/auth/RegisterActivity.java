package com.example.taskforge.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskforge.R;
import com.example.taskforge.data.entities.User;
import com.example.taskforge.domain.repositories.TaskForgeRepository;
import com.example.taskforge.domain.utils.Validator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private TaskForgeRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repository = new TaskForgeRepository(getApplication());

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> handleRegistration());

        tvGoToLogin.setOnClickListener(v -> finish()); // Return to LoginActivity
    }

    private void handleRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (!Validator.isNotEmpty(name)) {
            Toast.makeText(this, "Введіть ім'я", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Validator.isValidEmail(email)) {
            Toast.makeText(this, "Невірний формат email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Validator.isValidPassword(password)) {
            Toast.makeText(this, "Пароль має бути мінімум 6 символів", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Паролі не збігаються", Toast.LENGTH_SHORT).show();
            return;
        }

        // Перевіряємо чи існує користувач
        User existingUser = repository.getUserByEmail(email);
        if (existingUser != null) {
            Toast.makeText(this, "Користувач з таким email вже існує", Toast.LENGTH_SHORT).show();
            return;
        }

        String hashedPassword = hashPassword(password);
        User newUser = new User(name, email, hashedPassword);
        
        long result = repository.insertUser(newUser);

        if (result != -1) {
            Toast.makeText(this, "Успішна реєстрація! Тепер увійдіть.", Toast.LENGTH_LONG).show();
            finish(); // Proceed to login
        } else {
            Toast.makeText(this, "Помилка при реєстрації", Toast.LENGTH_SHORT).show();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }
}
