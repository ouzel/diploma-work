package com.example.recycleapp.data.model;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class User {

    private List<String> loginDates;
    private List<ScanningInfo> scanningInfo;
    private int points;

    public User(List<String> loginDates, List<ScanningInfo> scanningInfo, int points) {
        this.loginDates = loginDates != null ? loginDates : new ArrayList<>();
        this.scanningInfo = scanningInfo != null ? scanningInfo : new ArrayList<>();
        this.points = points;
    }

    public List<String> getLoginDates() {
        return loginDates;
    }

    public List<ScanningInfo> getScanningInfo() {
        return scanningInfo;
    }

    public int getPoints() {
        return points;
    }

    // ✅ Добавление нового сканирования без дубликатов
    public void addScanningInfo(String date, String result, String description) {
        if (date != null && result != null && description != null) {
            ScanningInfo newInfo = new ScanningInfo(date, result, description);
            if (!scanningInfo.contains(newInfo)) {
                this.scanningInfo.add(newInfo);
            }
        }
    }

    // ✅ Добавление даты входа без дубликатов
    public void addLoginDate(String date) {
        if (date != null && !date.isEmpty() && !this.loginDates.contains(date)) {
            this.loginDates.add(date);
        }
    }

    // 🚩 5. Печать количества переработок по категориям
    public Map<String, Integer> getRecyclingCountByCategory() {
        Map<String, Integer> categoryCount = new HashMap<>();
        categoryCount.put("plastic", 0);
        categoryCount.put("paper", 0);
        categoryCount.put("metal", 0);
        categoryCount.put("glass", 0);

        for (ScanningInfo info : scanningInfo) {
            String result = info.getResult().split("-")[0];
            if (Arrays.asList("01", "02", "03", "04", "05", "06", "07").contains(result)) {
                categoryCount.put("plastic", categoryCount.get("plastic") + 1);
            } else if (Arrays.asList("20", "21", "22").contains(result)) {
                categoryCount.put("paper", categoryCount.get("paper") + 1);
            } else if (Arrays.asList("40", "41").contains(result)) {
                categoryCount.put("metal", categoryCount.get("metal") + 1);
            } else if (Arrays.asList("70", "71", "72", "73", "74", "75", "76", "77", "78", "79").contains(result)) {
                categoryCount.put("glass", categoryCount.get("glass") + 1);
            }
        }
        return categoryCount;
    }

    // 🚩 Сохранение данных пользователя в JSON файл
    public boolean saveToJson(Context context, String fileName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("loginDates", new JSONArray(loginDates));

            JSONArray scanningInfoArray = new JSONArray();
            for (ScanningInfo info : scanningInfo) {
                JSONObject scanObject = new JSONObject();
                scanObject.put("date", info.getDate());
                scanObject.put("result", info.getResult());
                scanObject.put("description", info.getDescription());
                scanningInfoArray.put(scanObject);
            }
            jsonObject.put("scanningInfo", scanningInfoArray);
            jsonObject.put("points", points);

            String jsonString = jsonObject.toString();
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
            return true;

        } catch (JSONException | IOException e) {
            Log.e("User", "Failed to save JSON", e);
            return false;
        }
    }

    // 🚩 Загрузка данных пользователя из JSON файла
    public static User loadFromAssets(Context context, String fileName) {
        String jsonString = loadJSONFromAsset(context, fileName);
        if (jsonString == null) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            List<String> loginDates = new ArrayList<>();
            JSONArray loginDatesArray = jsonObject.getJSONArray("loginDates");
            for (int i = 0; i < loginDatesArray.length(); i++) {
                loginDates.add(loginDatesArray.getString(i));
            }

            List<ScanningInfo> scanningInfoList = new ArrayList<>();
            JSONArray scanningInfoArray = jsonObject.getJSONArray("scanningInfo");
            for (int i = 0; i < scanningInfoArray.length(); i++) {
                JSONObject scanObject = scanningInfoArray.getJSONObject(i);
                ScanningInfo scanningInfo = new ScanningInfo(
                        scanObject.getString("date"),
                        scanObject.getString("result"),
                        scanObject.getString("description")
                );
                scanningInfoList.add(scanningInfo);
            }

            int points = jsonObject.getInt("points");
            return new User(loginDates, scanningInfoList, points);
        } catch (JSONException e) {
            Log.e("User", "Failed to parse JSON", e);
            return null;
        }
    }

    private static String loadJSONFromAsset(Context context, String fileName) {
        try (InputStream is = context.openFileInput(fileName)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e("User", "Error reading file", ex);
            return null;
        }
    }

    public static class ScanningInfo {
        private String date;
        private String result;
        private String description;

        public ScanningInfo(String date, String result, String description) {
            this.date = date;
            this.result = result;
            this.description = description;
        }

        public String getDate() { return date; }
        public String getResult() { return result; }
        public String getDescription() { return description; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScanningInfo that = (ScanningInfo) o;
            return Objects.equals(date, that.date) &&
                    Objects.equals(result, that.result) &&
                    Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(date, result, description);
        }
    }
}



