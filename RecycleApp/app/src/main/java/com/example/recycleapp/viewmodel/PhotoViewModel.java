package com.example.recycleapp.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PhotoViewModel extends ViewModel {
    private final MutableLiveData<Uri> capturedPhotoUri = new MutableLiveData<>();

    public LiveData<Uri> getCapturedPhotoUri() {
        return capturedPhotoUri;
    }

    public void setCapturedPhotoUri(Uri uri) {
        capturedPhotoUri.postValue(uri);
    }
}

