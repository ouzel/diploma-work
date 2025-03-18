package com.example.recycleapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.recycleapp.R;
import com.example.recycleapp.data.model.Post;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;

public class AddPostDialog extends DialogFragment {

    private EditText titleEditText, textEditText;
    private Button publishButton, cancelButton;
    private CheckBox addLocationCheckBox;
    private LocationManager locationManager;
    private String currentLocation = null;

    public interface AddPostListener {
        void onPostAdded(Post post);
    }

    private AddPostListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AddPostListener) {
            listener = (AddPostListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AddPostListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_post, container, false);

        titleEditText = view.findViewById(R.id.et_post_title);
        textEditText = view.findViewById(R.id.et_post_text);
        publishButton = view.findViewById(R.id.btn_publish_post);
        cancelButton = view.findViewById(R.id.btn_cancel_post);
        addLocationCheckBox = view.findViewById(R.id.checkbox_add_location);

        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        publishButton.setOnClickListener(v -> publishPost());
        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            currentLocation = location.getLatitude() + ", " + location.getLongitude();
        }
    }

    private void publishPost() {
        String title = titleEditText.getText().toString();
        String text = textEditText.getText().toString();
        String author = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String publishedAt = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault())
                .format(new Date());

        if (addLocationCheckBox.isChecked()) {
            fetchCurrentLocation();
        }

        if (!title.isEmpty() && !text.isEmpty() && author != null) {
            Post post = new Post(title, text, author, currentLocation, 0, publishedAt);
            listener.onPostAdded(post);
            dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        }
    }
}