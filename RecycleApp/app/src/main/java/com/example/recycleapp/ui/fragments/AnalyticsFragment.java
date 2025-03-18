package com.example.recycleapp.ui.fragments;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;

import com.example.recycleapp.R;
import com.example.recycleapp.data.model.User;
import com.example.recycleapp.ui.CircularProgressView;
import com.example.recycleapp.ui.adapter.TimelineAdapter;
import com.example.recycleapp.viewmodel.UserViewModel;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnalyticsFragment extends Fragment {

    private CircularProgressView circularProgressView;
    private TextView textLevel;
    private TextView textProgress;
    private ImageView imageLevel;
    private UserViewModel userViewModel;
    private Button buttonViewHistory;
    private TimelineAdapter timelineAdapter;
    private TextView plasticCountView, glassCountView, metalCountView, paperCountView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        circularProgressView = view.findViewById(R.id.circularProgressView);
        textLevel = view.findViewById(R.id.textLevel);
        textProgress = view.findViewById(R.id.textProgress);
        imageLevel = view.findViewById(R.id.imageAnimation);

        // Найти TextView для каждой карточки
        plasticCountView = view.findViewById(R.id.plasticCount);
        glassCountView = view.findViewById(R.id.glassCount);
        metalCountView = view.findViewById(R.id.metalCount);
        paperCountView = view.findViewById(R.id.paperCount);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Наблюдение за изменениями данных пользователя
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // int points = user.getPoints();
                int points = userViewModel.getScanningInfo().size()*3 + 1;
                updateProgress(points);
                startImageAnimation(); // Запуск анимации картинки
            } else {
                Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show();
            }
        });

        buttonViewHistory = view.findViewById(R.id.buttonViewHistory);
        buttonViewHistory.setOnClickListener(v -> showTimelineDialog());

        // Получение данных о переработках по категориям
        Map<String, Integer> recyclingCountByCategory = userViewModel.getRecyclingCountByCategory();

        // Вызов метода для печати результатов
        printRecyclingCount(recyclingCountByCategory);

        // Установка текстовой подсказки
        TooltipCompat.setTooltipText(imageLevel, "Заходи каждый день, сканируй — получай баллы и открывай новые возможности!");

        // Альтернативный вариант с кликом (если нужно показать подсказку только по нажатию)
        imageLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageLevel.performLongClick(); // Программный вызов подсказки
            }
        });

        return view;
    }

    // Метод для печати результатов (пока TODO)
    private void printRecyclingCount(Map<String, Integer> recyclingCount) {

        // Установить текст с количеством переработок по категориям
        plasticCountView.setText(recyclingCount.getOrDefault("plastic", 0) + " шт.");
        glassCountView.setText(recyclingCount.getOrDefault("glass", 0) + " шт.");
        metalCountView.setText(recyclingCount.getOrDefault("metal", 0) + " шт.");
        paperCountView.setText(recyclingCount.getOrDefault("paper", 0) + " шт.");
    }


    private void showTimelineDialog() {
        // userViewModel.addScanningInfo("2025-02-24", "NEW", "WORKING");
        // Создание диалога
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_timeline);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerViewTimeline);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Инициализация адаптера с пустым списком и установка в RecyclerView
        timelineAdapter = new TimelineAdapter(userViewModel.getScanningInfoLiveData().getValue());
        recyclerView.setAdapter(timelineAdapter);

        userViewModel.getScanningInfoLiveData().observe(getViewLifecycleOwner(), new Observer<List<User.ScanningInfo>>() {
            @Override
            public void onChanged(List<User.ScanningInfo> scanningInfoList) {
                if (scanningInfoList != null) {
                    timelineAdapter.setScanningInfoList(scanningInfoList);
                }
            }
        });

        dialog.show();
    }


    private void updateProgress(int points) {
        ValueAnimator animator = ValueAnimator.ofInt(0, points % 50);
        animator.setDuration(3000); // Длительность анимации — 3 секунды
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            circularProgressView.setPoints(animatedValue);
        });
        animator.start();

        // Обновление уровня
        int level = points / 50 + 1;
        textLevel.setText("Уровень " + level);
        textProgress.setText(points%50 + "/50");
    }

    private void startImageAnimation() {
        // Анимация пульсации (увеличение и уменьшение)
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageLevel, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageLevel, "scaleY", 1f, 1.1f, 1f);

        // Анимация лёгкого покачивания (повороты)
        ObjectAnimator rotation = ObjectAnimator.ofFloat(imageLevel, "rotation", 0f, 5f, -5f, 0f);

        // Настройки анимации
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        rotation.setDuration(3000);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        rotation.setRepeatCount(ValueAnimator.INFINITE);

        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        rotation.setRepeatMode(ValueAnimator.REVERSE);

        // Объединение анимаций
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, rotation);
        animatorSet.start();
    }
}

