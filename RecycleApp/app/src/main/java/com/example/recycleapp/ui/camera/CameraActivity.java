package com.example.recycleapp.ui.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.recycleapp.BuildConfig;
import com.example.recycleapp.R;
import com.example.recycleapp.viewmodel.PhotoViewModel;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

public class CameraActivity extends AppCompatActivity {

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private PhotoViewModel photoViewModel;
    private PreviewView viewFinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Инициализация ExecutorService
        cameraExecutor = Executors.newSingleThreadExecutor();
        photoViewModel = new ViewModelProvider(this).get(PhotoViewModel.class);

        // Найти PreviewView в XML
        viewFinder = findViewById(R.id.camera_preview);

        Button captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(v -> {
            if (imageCapture != null) {
                savePhotoToAppDirectory();
            } else {
                Toast.makeText(CameraActivity.this, "Камера не готова", Toast.LENGTH_SHORT).show();
            }
        });

        // Проверка разрешений
        if (checkPermissions()) {
            startCamera();
        } else {
            requestPermissions();
        }
    }

    private void savePhotoToAppDirectory() {
        // Определяем директорию для сохранения изображений
        File photoDirectory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "RecycleApp");
        if (!photoDirectory.exists() && !photoDirectory.mkdirs()) {
            Log.e("CameraActivity", "Failed to create directory for saving photos.");
            Toast.makeText(this, "Не удалось создать директорию для сохранения", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создаем файл для сохранения изображения
        String fileName = "photo_" + System.currentTimeMillis() + ".jpg";
        File photoFile = new File(photoDirectory, fileName);
        Uri photoUri;

        try {
            photoUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);
        } catch (IllegalArgumentException e) {
            Log.e("CameraActivity", "Error creating URI for photo", e);
            Toast.makeText(this, "Ошибка при создании URI", Toast.LENGTH_SHORT).show();
            return;
        }

        // Настройка параметров сохранения
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Сохраняем изображение
        imageCapture.takePicture(options, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Log.d("CameraActivity", "Photo saved successfully: " + photoUri.toString());

                // Сохраняем URI в ViewModel
                photoViewModel.setCapturedPhotoUri(photoUri);

                // Переход на экран редактирования
                Intent intent = new Intent(CameraActivity.this, PhotoEditActivity.class);
                intent.putExtra("image_uri", photoUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraActivity", "Image capture error: " + exception.getImageCaptureError());
                Log.e("CameraActivity", "Error message: " + exception.getMessage(), exception);

                runOnUiThread(() -> Toast.makeText(
                        CameraActivity.this,
                        "Ошибка сохранения: " + exception.getMessage() +
                                " (Код ошибки: " + exception.getImageCaptureError() + ")",
                        Toast.LENGTH_LONG).show()
                );
            }
        });
    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Создание Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // Создание ImageCapture
                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(viewFinder.getDisplay().getRotation()) // Учитывайте ориентацию
                        .build();

                // Выбор камеры
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Привязка к жизненному циклу
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Log.e("CameraActivity", "Camera initialization failed: " + e.getMessage(), e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}



