package com.example.recycleapp.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.recycleapp.R;
import com.example.recycleapp.data.NotificationWorker;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }
        scheduleNotifications();

        // Инициализация FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        EditText emailField = findViewById(R.id.emailField);
        EditText passwordField = findViewById(R.id.passwordField);
        Button registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(view -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            // Проверка на пустые поля
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            // Регистрация пользователя
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            scheduleNotifications();
                            // Отправка письма с подтверждением
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(emailTask -> {
                                            if (emailTask.isSuccessful()) {
                                                Toast.makeText(this, "Регистрация успешна! Подтвердите email перед входом.", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(this, "Ошибка отправки email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            finish(); // Закрыть экран после регистрации
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Ошибка";
                            Toast.makeText(this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void scheduleNotifications() {
        long initialDelay = TimeUnit.DAYS.toMillis(14);
        long interval = TimeUnit.DAYS.toMillis(14);
        int notificationCount = 12; // 6 месяцев, каждые 14 дней

        for (int i = 0; i < notificationCount; i++) {
            long delay = initialDelay + (i * interval);

            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build();

            WorkManager.getInstance(this).enqueue(notificationWork);
        }
    }
}


