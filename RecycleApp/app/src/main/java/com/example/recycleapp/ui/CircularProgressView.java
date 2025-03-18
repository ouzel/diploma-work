package com.example.recycleapp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class CircularProgressView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF;

    private int maxPoints = 50;
    private int points = 0;

    private ImageView imageView;
    private TextView textView;

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFFE0E0E0); // Серый фон
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(30f);
        backgroundPaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setColor(0xFF4CAF50); // Зеленый прогресс
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(30f);
        progressPaint.setAntiAlias(true);

        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int minDimension = Math.min(width, height);

        float padding = 40f;
        rectF.set(padding, padding, minDimension - padding, minDimension - padding);

        // Рисуем серый фон круга
        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);

        // Вычисляем угол для заполнения прогрессом
        float angle = 360 * (points / (float) maxPoints);
        canvas.drawArc(rectF, -90, angle, false, progressPaint);
    }

    public void setPoints(int points) {
        this.points = Math.min(points, maxPoints);
        invalidate(); // Перерисовка View
    }
}

