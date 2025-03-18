package com.example.recycleapp.ui.navigation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.recycleapp.R;

import com.example.recycleapp.data.model.Post;
import com.example.recycleapp.ui.AddPostDialog;
import com.example.recycleapp.viewmodel.NewsViewModel;
import com.google.android.material.navigation.NavigationView;

public class NavigationDrawerActivity extends AppCompatActivity implements AddPostDialog.AddPostListener {

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // DrawerLayout и NavigationView
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Конфигурация AppBar
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_scan, R.id.nav_map, R.id.nav_news, R.id.nav_analytics)
                .build();

        // Настройка NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Получаем данные из Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("target_fragment") && intent.hasExtra("scan_message")) {
            int targetFragmentId = intent.getIntExtra("target_fragment", R.id.nav_news);
            String scanMessage = intent.getStringExtra("scan_message");

            // Передача аргумента в NavController
            Bundle args = new Bundle();
            args.putString("scan_message", scanMessage);
            navController.navigate(targetFragmentId, args);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onPostAdded(Post post) {
        // Логика добавления поста
        NewsViewModel viewModel = new ViewModelProvider(this).get(NewsViewModel.class);
        viewModel.addPost(post);
    }
}

