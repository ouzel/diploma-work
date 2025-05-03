package com.example.recycleapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.recycleapp.R;
import com.example.recycleapp.ui.auth.LoginActivity;
import com.example.recycleapp.ui.auth.RegisterActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        Log.d("DEBUG", "Layout set successfully");

        mAuth = FirebaseAuth.getInstance();

        // Проверка, авторизован ли пользователь
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Если пользователь уже авторизован, переходим на главную страницу
            //navigateToMainMenu();
            //return;
        }

        Button loginButton = findViewById(R.id.loginButton);

        Button registerButton = findViewById(R.id.openRegisterButton);
        if (registerButton == null) {
            Log.e("DEBUG", "Button openRegisterButton not found");
        } else {
            Log.d("DEBUG", "Button openRegisterButton found");
        }

        // Переход на экран входа
        loginButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Переход на экран регистрации
        try {
            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });
        } catch (NullPointerException e) {
            Log.e("ERROR", "NullPointerException: " + e.getMessage());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Проверяем статус пользователя при запуске
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //navigateToMainMenu();
        }
    }

//    private void navigateToMainMenu() {
//        Intent intent = new Intent(MainActivity.this, NavigationDrawerActivity.class);
//        startActivity(intent);
//        finish(); // Завершаем MainActivity, чтобы пользователь не мог вернуться назад
//    }
}