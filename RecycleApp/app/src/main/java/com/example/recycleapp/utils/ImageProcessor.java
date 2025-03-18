package com.example.recycleapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageProcessor {

    private static final int IMAGE_SIZE = 224;

    /**
     * Обработка изображения: изменение размера и преобразование в ByteBuffer
     *
     * @param imagePath Путь к изображению.
     * @return Предобработанный ByteBuffer для модели.
     * @throws IOException Если файл не найден или ошибка загрузки изображения.
     */
    public ByteBuffer processImage(String imagePath) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        if (bitmap == null) {
            throw new IOException("Не удалось загрузить изображение по пути: " + imagePath);
        }

        // Изменение размера изображения до 224x224
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);

        // Создание ByteBuffer для модели (uint8)
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * 3);
        inputBuffer.order(ByteOrder.nativeOrder());

        // Преобразование пикселей в ByteBuffer
        for (int y = 0; y < IMAGE_SIZE; y++) {
            for (int x = 0; x < IMAGE_SIZE; x++) {
                int pixel = resizedBitmap.getPixel(x, y);
                inputBuffer.put((byte) ((pixel >> 16) & 0xFF)); // Красный
                inputBuffer.put((byte) ((pixel >> 8) & 0xFF));  // Зеленый
                inputBuffer.put((byte) (pixel & 0xFF));         // Синий
            }
        }

        return inputBuffer;
    }

    public Bitmap byteBufferToBitmap(ByteBuffer byteBuffer) {
        Bitmap bitmap = Bitmap.createBitmap(IMAGE_SIZE, IMAGE_SIZE, Bitmap.Config.ARGB_8888);
        byteBuffer.rewind(); // Убедимся, что указатель ByteBuffer находится в начале
        for (int y = 0; y < IMAGE_SIZE; y++) {
            for (int x = 0; x < IMAGE_SIZE; x++) {
                int r = byteBuffer.get() & 0xFF; // Red
                int g = byteBuffer.get() & 0xFF; // Green
                int b = byteBuffer.get() & 0xFF; // Blue
                int color = Color.rgb(r, g, b);
                bitmap.setPixel(x, y, color);
            }
        }
        return bitmap;
    }

    public Bitmap processImageToBitmap(String imagePath) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        if (bitmap == null) {
            throw new IOException("Не удалось загрузить изображение по пути: " + imagePath);
        }

        return Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
    }
}



