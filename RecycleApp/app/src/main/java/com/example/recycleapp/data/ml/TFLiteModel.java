package com.example.recycleapp.data.ml;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class TFLiteModel {
    private Interpreter interpreter;

    public TFLiteModel(AssetManager assetManager, String modelPath) throws IOException {
        ByteBuffer modelBuffer = loadModelFile(assetManager, modelPath);
        Interpreter.Options options = new Interpreter.Options();
        options.setUseNNAPI(true);
        options.setNumThreads(4);
        options.setAllowBufferHandleOutput(true);
        interpreter = new Interpreter(modelBuffer, options);
    }

    private ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        try (InputStream is = assetManager.open(modelPath);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(baos.size());
            byteBuffer.order(ByteOrder.nativeOrder());
            byteBuffer.put(baos.toByteArray());
            return byteBuffer;
        }
    }

    public int predict(Bitmap bitmap) {
        Log.d("TFLiteModel", "Начало обработки модели...");
        if (bitmap == null) {
            throw new IllegalArgumentException("Bitmap не может быть null");
        }

        try {
            Log.d("TFLiteModel", "Размер изображения (ширина x высота): " + bitmap.getWidth() + " x " + bitmap.getHeight());

            // Изменение размера изображения до (224, 224)
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

            // Преобразуем Bitmap в ByteBuffer
            ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);

            // Проверяем, что преобразование прошло успешно
            if (inputBuffer == null) {
                throw new IllegalStateException("Ошибка: ByteBuffer оказался null после преобразования Bitmap.");
            }

            // Создаем массив для хранения результата
            float[][] output = new float[1][31]; // Массив для 31 класса

            // Проверяем, инициализирован ли интерпретатор
            if (interpreter == null) {
                throw new IllegalStateException("Ошибка: Interpreter не инициализирован. Убедитесь, что модель загружена корректно.");
            }

            Log.d("TFLiteModel", "Начинаем предсказание...");

            // Выполняем предсказание
            interpreter.run(inputBuffer, output);

            Log.d("TFLiteModel", "Предсказание выполнено.");
            Log.d("TFLiteModel", "Выходной массив вероятностей: " + Arrays.toString(output[0]));

            // Находим индекс с максимальным значением (класс с наибольшей вероятностью)
            int predictedClass = -1;
            float maxProbability = -1;
            for (int i = 0; i < output[0].length; i++) {
                if (output[0][i] > maxProbability) {
                    maxProbability = output[0][i];
                    predictedClass = i;
                }
            }

            Log.d("TFLiteModel", "Максимальная вероятность: " + maxProbability);
            Log.d("TFLiteModel", "Предсказанный класс: " + predictedClass);

            // Возвращаем предсказанный класс
            if (predictedClass != -1) {
                return predictedClass;
            } else {
                throw new IllegalStateException("Ошибка: Не удалось предсказать класс.");
            }

        } catch (IllegalArgumentException e) {
            Log.e("TFLiteModel", "Неверный аргумент: " + e.getMessage());
        } catch (IllegalStateException e) {
            Log.e("TFLiteModel", "Состояние модели некорректно: " + e.getMessage());
        } catch (Exception e) {
            Log.e("TFLiteModel", "Общая ошибка предсказания: " + e.getMessage(), e);
        }

        // Возвращаем -1 в случае ошибки
        return -1;
    }

    /**
     * Преобразует Bitmap в ByteBuffer, подходящий для модели TensorFlow Lite.
     */
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3); // Размер для (224, 224, 3)
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[224 * 224];
        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224);

        // Преобразуем пиксели в нормализованные значения (0.0 - 1.0)
        for (int pixelValue : intValues) {
            float red = ((pixelValue >> 16) & 0xFF) / 255.0f;
            float green = ((pixelValue >> 8) & 0xFF) / 255.0f;
            float blue = (pixelValue & 0xFF) / 255.0f;

            byteBuffer.putFloat(red);
            byteBuffer.putFloat(green);
            byteBuffer.putFloat(blue);
        }

        return byteBuffer;
    }


    private ByteBuffer convertBitmapToByteBuffer2(Bitmap bitmap) {
        int inputSize = 224; // Размер входа модели
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4); // UINT8 (1 байт на цвет)
        byteBuffer.order(ByteOrder.nativeOrder());

        // Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize , inputSize, true);
        Bitmap resizedBitmap = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resizedBitmap);
        canvas.drawBitmap(bitmap, null, new Rect(0, 0, inputSize, inputSize), null);

        int[] intValues = new int[inputSize * inputSize];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());

        for (int pixelValue : intValues) {
            byteBuffer.put((byte) ((pixelValue >> 16) & 0xFF)); // Красный
            byteBuffer.put((byte) ((pixelValue >> 8) & 0xFF));  // Зеленый
            byteBuffer.put((byte) (pixelValue & 0xFF));         // Синий
        }
        return byteBuffer;
    }


    public void close() {
        if (interpreter != null) {
            interpreter.close();
        }
    }
}


