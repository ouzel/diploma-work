package com.example.recycleapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.recycleapp.data.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserViewModel extends AndroidViewModel {

    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User.ScanningInfo>> scanningInfoLiveData = new MutableLiveData<>();
    private final String FILE_NAME = "user.json";

    public UserViewModel(@NonNull Application application) {
        super(application);
        loadUserData();
    }

    private void loadUserData() {
        User user = User.loadFromAssets(getApplication(), "user.json");
        if (user == null) {
            user = new User(new ArrayList<>(), new ArrayList<>(), 0);
        }
        userLiveData.setValue(user);
        scanningInfoLiveData.setValue(user.getScanningInfo()); // Инициализация scanningInfoLiveData
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public void addScanningInfo(String date, String result, String description) {
        User user = userLiveData.getValue();
        if (user != null) {
            user.addScanningInfo(date, result, description);
            userLiveData.setValue(user);
            saveUserData();
        }
    }

    public void addLoginDate(String date) {
        User user = userLiveData.getValue();
        if (user != null) {
            user.addLoginDate(date);
            userLiveData.setValue(user);
            saveUserData();
        }
    }

    private void saveUserData() {
        User user = userLiveData.getValue();
        if (user != null) {
            user.saveToJson(getApplication(), FILE_NAME);
        }
    }


    // 🚩 5. Печать количества переработок по категориям
    public Map<String, Integer> getRecyclingCountByCategory() {
        User user = userLiveData.getValue();
        return user != null ? user.getRecyclingCountByCategory() : new HashMap<>();
    }

    public List<String> getLoginDates() {
        User user = userLiveData.getValue();
        return user != null ? user.getLoginDates() : null;
    }

    public List<User.ScanningInfo> getScanningInfo() {
        User user = userLiveData.getValue();
        return user != null ? user.getScanningInfo() : null;
    }

    public LiveData<List<User.ScanningInfo>> getScanningInfoLiveData() {
        return scanningInfoLiveData;
    }
}


