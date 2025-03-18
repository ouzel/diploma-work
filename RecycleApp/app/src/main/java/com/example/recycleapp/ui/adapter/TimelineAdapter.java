package com.example.recycleapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycleapp.R;
import com.example.recycleapp.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private final List<User.ScanningInfo> scanningInfoList = new ArrayList<>();

    public TimelineAdapter(List<User.ScanningInfo> initialScanningInfoList) {
        if (initialScanningInfoList != null) {
            scanningInfoList.addAll(initialScanningInfoList);
        }
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        User.ScanningInfo info = scanningInfoList.get(getItemCount() - 1 - position);
        holder.textDate.setText(info.getDate());
        holder.textItemName.setText(info.getResult());
        holder.textItemDescription.setText(info.getDescription());
    }

    @Override
    public int getItemCount() {
        return scanningInfoList.size();
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView textDate, textItemName, textItemDescription;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textDate);
            textItemName = itemView.findViewById(R.id.textItemName);
            textItemDescription = itemView.findViewById(R.id.textItemDescription);
        }
    }

    // Обновляет список объектов ScanningInfo и оповещает адаптер об изменении данных
    public void setScanningInfoList(List<User.ScanningInfo> newScanningInfoList) {
        scanningInfoList.clear();
        if (newScanningInfoList != null) {
            scanningInfoList.addAll(newScanningInfoList);
        }
        notifyDataSetChanged(); // Или используйте notifyItemRangeChanged(0, scanningInfoList.size());
    }

    //обавляет новый объект ScanningInfo в список и обновляет адаптер.
    public void addScanningInfo(User.ScanningInfo scanningInfo) {
        if (scanningInfo != null) {
            scanningInfoList.add(scanningInfo);
            notifyItemInserted(scanningInfoList.size() - 1);
        }
    }

    /**
     * Очищает список объектов ScanningInfo.
     */
    public void clearScanningInfo() {
        scanningInfoList.clear();
        notifyDataSetChanged();
    }
}

