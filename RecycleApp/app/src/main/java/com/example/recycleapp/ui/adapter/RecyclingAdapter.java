package com.example.recycleapp.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycleapp.R;
import com.example.recycleapp.data.RecyclingCodeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class RecyclingAdapter extends RecyclerView.Adapter<RecyclingAdapter.RecyclingViewHolder> {

    private final List<RecyclingCodeHandler.RecyclingCode> recyclingCodes;

    public RecyclingAdapter(List<RecyclingCodeHandler.RecyclingCode> recyclingCodes) {
        this.recyclingCodes = recyclingCodes;
    }

    @NonNull
    @Override
    public RecyclingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new RecyclingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclingViewHolder holder, int position) {
        RecyclingCodeHandler.RecyclingCode code = recyclingCodes.get(position);

        // Установка текста для всех TextView
        holder.materialTextView.setText("Код: " + code.getCodeNumber() + " | " + code.getMaterial());
        holder.descriptionTextView.setText(code.getDescription());
        holder.recommendationsTextView.setText(code.getRecyclingRecommendations());
        holder.materialTypeTextView.setText("Тип материала: " + code.getMaterialType());
        holder.examplesTextView.setText("" + String.join(", ", code.getExamples()));
        holder.notesTextView.setText("" + code.getNotes());

        // Установка изображения из assets, если оно существует
        String imagePath = "recycling_code_pictures/" + code.getImagePath(); // Пример: "003_pvc.png"
        try (InputStream inputStream = holder.itemView.getContext().getAssets().open(imagePath)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            holder.materialImageView.setVisibility(View.VISIBLE);
            holder.materialImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            // Если файл не найден, просто скрываем ImageView
            holder.materialImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return recyclingCodes.size();
    }

    public static class RecyclingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView materialImageView;
        private final TextView materialTextView;
        private final TextView descriptionTextView;
        private final TextView recommendationsTextView;
        private final TextView materialTypeTextView;
        private final TextView examplesTextView;
        private final TextView notesTextView;

        public RecyclingViewHolder(@NonNull View itemView) {
            super(itemView);
            materialImageView = itemView.findViewById(R.id.image_material);
            materialTextView = itemView.findViewById(R.id.text_material);
            descriptionTextView = itemView.findViewById(R.id.text_description);
            recommendationsTextView = itemView.findViewById(R.id.text_recycling_recommendations);
            materialTypeTextView = itemView.findViewById(R.id.text_material_type);
            examplesTextView = itemView.findViewById(R.id.text_examples);
            notesTextView = itemView.findViewById(R.id.text_notes);
        }
    }
}


