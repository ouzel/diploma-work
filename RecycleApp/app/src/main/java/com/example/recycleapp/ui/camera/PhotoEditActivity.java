package com.example.recycleapp.ui.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.recycleapp.R;
import com.example.recycleapp.data.ml.TFLiteModel;
import com.example.recycleapp.ui.navigation.NavigationDrawerActivity;
import com.example.recycleapp.utils.ImageProcessor;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;

public class PhotoEditActivity extends AppCompatActivity {

    private static final String SAMPLE_CROPPED_IMAGE_NAME = "CroppedImage";
    private static final int IMAGE_SIZE = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Укажите явно super из AppCompatActivity
        setContentView(R.layout.activity_photo_edit);

        Uri sourceUri = getIntentImageUri();
        if (sourceUri != null) {
            startCrop(sourceUri);
        } else {
            handleImageUriError();
        }
    }

    private Uri getIntentImageUri() {

        if (getIntent() != null && getIntent().hasExtra("image_uri")) {
            return getIntent().getParcelableExtra("image_uri");
        }
        return null;
    }

    private void handleImageUriError() {
        Toast.makeText(this, "Ошибка: изображение не передано", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void startCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), SAMPLE_CROPPED_IMAGE_NAME + ".jpg"));
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);
        options.setHideBottomControls(true);
        options.setFreeStyleCropEnabled(true);

        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(IMAGE_SIZE, IMAGE_SIZE)
                .withOptions(options)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                Uri croppedImageUri = UCrop.getOutput(data);
                if (croppedImageUri != null) {
                    try {
                        ImageProcessor imageProcessor = new ImageProcessor();
                        Bitmap processedBitmap = imageProcessor.processImageToBitmap(croppedImageUri.getPath());
                        // Передача обработанного изображения в классификатор
                        classifyImage(processedBitmap);

                        // TODO del and uncomment
                        // InputStream inputStream = getAssets().open("tt22.png");
                        // Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        // Uri imageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null));
                        // classifyImage(bitmap);
                        // TODO del and uncomment

                    } catch (IOException e) {
                        Log.e("PhotoEditActivity", "Ошибка обработки изображения: " + e.getMessage());
                        Toast.makeText(this, "Ошибка обработки изображения", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Ошибка обрезки", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Throwable cropError = UCrop.getError(data);
                Toast.makeText(this, "Ошибка: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void classifyImage(Bitmap processedBitmap) {
        try {
            TFLiteModel model = new TFLiteModel(getAssets(), "model.tflite");

            // Получение предсказания

            int prediction = model.predict(processedBitmap);
            if (prediction != 32) {
                Log.d("TFLite", "Предсказание: " + prediction);
                navigateToScanFragmentWithResult(String.valueOf(prediction));
            } else {
                Log.e("TFLite", "Предсказание не распознано");
                navigateToScanFragmentWithResult("Не распознано");
            }

            // Закрытие модели
            model.close();
        } catch (IOException e) {
            Log.e("TFLite", "Ошибка загрузки модели: " + e.getMessage());
        } catch (Exception e) {
            Log.e("TFLite", "Ошибка предсказания: " + e.getMessage());
        }
    }

    private void navigateToScanFragmentWithResult(String resultMessage) {
        Intent intent = new Intent(PhotoEditActivity.this, NavigationDrawerActivity.class);
        intent.putExtra("scan_message", resultMessage);
        intent.putExtra("target_fragment", R.id.nav_scan);
        startActivity(intent);
        finish();
    }
}



