package com.example.recycleapp.data;

import android.content.Context;
import android.util.Log;

import com.example.recycleapp.data.model.RecyclingPoint;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class RecyclingRepository {
    private Context context;

    public RecyclingRepository(Context context) {
        this.context = context;
    }

    public List<RecyclingPoint> getRecyclingPoints() {
        try (InputStream is = context.getAssets().open("recycling_points.json");
             InputStreamReader reader = new InputStreamReader(is)) {
            Type type = new TypeToken<List<RecyclingPoint>>() {
            }.getType();

            // List<RecyclingPoint> pp = new Gson().fromJson(reader, type);
            // Log.d("RecyclingPoints", "total number: " + pp.size());

            return new Gson().fromJson(reader, type);
        } catch (Exception e) {
            Log.e("Repository", "JSON parsing error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

