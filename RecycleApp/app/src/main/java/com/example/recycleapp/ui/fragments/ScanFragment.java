package com.example.recycleapp.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recycleapp.data.RecyclingCodeHandler;
import com.example.recycleapp.data.model.User;
import com.example.recycleapp.ui.adapter.RecyclingAdapter;
import com.example.recycleapp.ui.camera.CameraActivity;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycleapp.R;
import com.example.recycleapp.viewmodel.UserViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ScanFragment extends Fragment {

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private RecyclerView recyclerView;
    private RecyclingCodeHandler handler;
    private RecyclingAdapter adapter;
    private UserViewModel userViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Инициализация requestPermissionLauncher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(requireContext(), "Доступ к камере предоставлен", Toast.LENGTH_SHORT).show();
                        showScanInfoDialog(); // Показываем информационное окно перед камерой
                    } else {
                        Toast.makeText(requireContext(), "Камера недоступна без разрешения", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        // TextView для отображения результата
        TextView resultTextView = view.findViewById(R.id.text_result);
        // EditText для ввода текста
        EditText searchEditText = view.findViewById(R.id.edit_search);
        // Button для запуска поиска
        Button searchButton = view.findViewById(R.id.button_search);

        // RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        handler = new RecyclingCodeHandler(requireContext(), "recycling_codes.json");
        printDefaultPage();

        // Обработка нажатия кнопки поиска
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                printSearchResults(query);
                resultTextView.setText("Поиск выполнен: " + query);
                searchEditText.setHint("Введите текст для поиска");
            } else {
                resultTextView.setText("Введите текст для поиска");
            }
        });

        // Получение данных из NavController или аргументов
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("scan_message")) {
            String scanMessage = arguments.getString("scan_message");
            resultTextView.setText(scanMessage);
            if (!Objects.equals(scanMessage, "Сканирование не выполнено")) {
                printScanResults(scanMessage);
                searchEditText.setHint("<Картинка>");
            } else {
                searchEditText.setHint("Введите текст для поиска");
            }
        } else {
            resultTextView.setText("Результат сканирования будет отображен здесь");
        }

        // Кнопка для сканирования
        Button scanButton = view.findViewById(R.id.button_scan);
        scanButton.setOnClickListener(v -> {
            if (checkCameraPermissions()) {
                showScanInfoDialog(); // Показываем окно перед камерой
            } else {
                requestCameraPermissions();
            }
        });

        return view;
    }

    private void printSearchResults(String query) {
        List<RecyclingCodeHandler.RecyclingCode> filteredCodes;

        if (query.startsWith("\"") && query.endsWith("\"")) {
            String exactQuery = query.substring(1, query.length() - 1).trim();
            filteredCodes = handler.search(exactQuery, 0.0);
        } else {
            filteredCodes = handler.search(query, 0.3);
        }

        if (filteredCodes.isEmpty()) {
            Toast.makeText(requireContext(), "Поиск не выдал результатов", Toast.LENGTH_SHORT).show();
            adapter = new RecyclingAdapter(new ArrayList<>());
        } else {
            adapter = new RecyclingAdapter(filteredCodes);
        }

        recyclerView.setAdapter(adapter);
    }

    private void printScanResults(String scanMessage) {
        try {
            int number = Integer.parseInt(scanMessage);
            RecyclingCodeHandler.RecyclingCode code = handler.getByTfliteClassId(number);
            if (code != null) {
                List<RecyclingCodeHandler.RecyclingCode> filteredCodes = Arrays.asList(code);
                adapter = new RecyclingAdapter(filteredCodes);
                recyclerView.setAdapter(adapter);

                // Save scan result to JSON file
                saveScanResult(code);
                return;
            } else {
                adapter = new RecyclingAdapter(new ArrayList<>());
                recyclerView.setAdapter(adapter);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Ошибка при сканировании", Toast.LENGTH_SHORT).show();
            printDefaultPage();
        }
    }

    private void saveScanResult(RecyclingCodeHandler.RecyclingCode code) {
        try {
            // Формат даты dd.MM.yyyy
            String currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());

            String scanResult = code.getCodeNumber() + "-" + code.getMaterial();
            String description = code.getMaterialType();

            // Получение UserViewModel
            // userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

            // Добавление нового объекта в scanningInfo через ViewModel
            userViewModel.addScanningInfo(currentDate, scanResult, description);

            Log.d("UserViewModelll", "New scanning info added: " + currentDate + ", " + scanResult + ", " + description);
            Log.d("UserViewModelll", "Total items in scanningInfo: " + userViewModel.getScanningInfo().size());

            // Отображение уведомления об успешном сохранении
            Toast.makeText(getContext(), "Данные успешно сохранены!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка при сохранении данных!", Toast.LENGTH_SHORT).show();
        }
    }



    private void printDefaultPage() {
        adapter = new RecyclingAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private boolean checkCameraPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void openCamera() {
        Intent intent = new Intent(requireContext(), CameraActivity.class);
        startActivity(intent);
    }

    private void showScanInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_scan_info, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button okButton = dialogView.findViewById(R.id.button_ok);
        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            openCamera();
        });
        dialog.show();
    }
}

