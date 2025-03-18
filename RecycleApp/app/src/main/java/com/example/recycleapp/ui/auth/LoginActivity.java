package com.example.recycleapp.ui.auth;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.recycleapp.R;
import com.example.recycleapp.ui.navigation.NavigationDrawerActivity;
import com.example.recycleapp.viewmodel.LoginViewModel;
import com.example.recycleapp.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel loginViewModel;
    private UserViewModel userViewModel;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        EditText emailField = findViewById(R.id.emailField);
        EditText passwordField = findViewById(R.id.passwordField);
        Button loginButton = findViewById(R.id.loginButton);

        // Test data for quick login
        emailField.setText("sadrozd@edu.hse.ru");
        passwordField.setText("123456");

        // Observe login result
        loginViewModel.getLoginResult().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Вход выполнен!", Toast.LENGTH_SHORT).show();

                // Add the current date to login dates
                addLoginDate();
                showDailyStreakDialog(); // Show daily streak dialog
                //startActivity(new Intent(LoginActivity.this, NavigationDrawerActivity.class));
                //finish();
            } else {
                Toast.makeText(this, "Ошибка входа. Проверьте данные.", Toast.LENGTH_SHORT).show();
            }
        });

        // Login button click handler
        loginButton.setOnClickListener(view -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase authentication
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                loginViewModel.login(email, password);
                            } else {
                                Toast.makeText(this, "Подтвердите email перед входом!", Toast.LENGTH_LONG).show();
                                FirebaseAuth.getInstance().signOut(); // Sign out user if email not verified
                            }
                        } else {
                            Toast.makeText(this, "Ошибка входа. Проверьте данные.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void addLoginDate() {
        String currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

        List<String> loginDates = userViewModel.getLoginDates();
        if (loginDates != null) {
            Set<String> uniqueDates = new HashSet<>(loginDates);

            if (!uniqueDates.contains(currentDate)) {
                loginDates.add(currentDate);
                Toast.makeText(this, "Daily streak!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDailyStreakDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.daily_streak);
        dialog.setCancelable(true);

        ImageButton streakDay1 = dialog.findViewById(R.id.streak_day1);
        Button collectRewardButton = dialog.findViewById(R.id.buttonCollectReward);

        // Анимация для streak_day1
        streakDay1.setScaleX(1f);
        streakDay1.setScaleY(1f);
        streakDay1.setImageResource(R.drawable.like_down);
        streakDay1.post(() -> {
            streakDay1.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(1000)
                    .withEndAction(() -> {
                        streakDay1.setImageResource(R.drawable.like_up);
                        streakDay1.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(2000)
                                .start();
                    })
                    .start();
        });

        // Анимация для кнопки collectRewardButton
        collectRewardButton.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(LoginActivity.this, NavigationDrawerActivity.class));
            finish();
        });

        dialog.show();
    }

}
