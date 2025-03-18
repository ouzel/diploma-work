package com.example.recycleapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.recycleapp.data.model.RecyclingPoint;
import com.example.recycleapp.data.RecyclingRepository;

import java.util.List;

public class MapViewModel extends AndroidViewModel {
    private RecyclingRepository repository;
    private MutableLiveData<List<RecyclingPoint>> recyclingPoints;

    public MapViewModel(@NonNull Application application) {
        super(application);
        repository = new RecyclingRepository(application);
        recyclingPoints = new MutableLiveData<>();
        loadRecyclingPoints();
    }

    private void loadRecyclingPoints() {
        recyclingPoints.setValue(repository.getRecyclingPoints());
    }

    public LiveData<List<RecyclingPoint>> getRecyclingPoints() {
        return recyclingPoints;
    }
}

