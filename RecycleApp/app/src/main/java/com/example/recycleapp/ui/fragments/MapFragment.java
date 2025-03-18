package com.example.recycleapp.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.recycleapp.R;
import com.example.recycleapp.data.FavoritesManager;
import com.example.recycleapp.data.model.RecyclingPoint;
import com.example.recycleapp.viewmodel.MapViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapViewModel viewModel;
    private boolean showOnlyFavorites = false;

    public MapFragment() {
        super(R.layout.fragment_map);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Инициализация карты
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Установка пользовательского InfoWindow
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        mMap.setOnInfoWindowClickListener(marker -> {
            // При клике на InfoWindow откроется страница с подробностями
            showBottomSheet(marker);
        });

        // Включаем отображение текущего местоположения
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Центрируем карту на каком-либо месте (например, Москва)
        LatLng moscow = new LatLng(55.7558, 37.6173);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moscow, 10));

        viewModel.getRecyclingPoints().observe(getViewLifecycleOwner(), points -> {
            if (points != null && !points.isEmpty()) {
                Log.d("MapFragment", "Points loaded: " + points.size());
                addMarkers(googleMap, points);
            } else {
                Log.d("MapFragment", "No points to display or null list received.");
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MapViewModel.class);

        ImageButton zoomInButton = view.findViewById(R.id.btn_zoom_in);
        ImageButton zoomOutButton = view.findViewById(R.id.btn_zoom_out);
        ImageButton toggleFavoritesButton = view.findViewById(R.id.btn_toggle_favorites);


        // Найти чекбоксы
        CheckBox plasticFilter = view.findViewById(R.id.filter_plastic);
        CheckBox glassFilter = view.findViewById(R.id.filter_glass);
        CheckBox paperFilter = view.findViewById(R.id.filter_paper);
        CheckBox metalFilter = view.findViewById(R.id.filter_metal);

        // Обработчик изменения состояния чекбоксов
        CompoundButton.OnCheckedChangeListener filterChangeListener = (buttonView, isChecked) -> {
            Set<String> selectedMaterials = new HashSet<>();
            if (plasticFilter.isChecked()) selectedMaterials.add("пластик");
            if (glassFilter.isChecked()) selectedMaterials.add("стекло");
            if (paperFilter.isChecked()) selectedMaterials.add("бумага");
            if (metalFilter.isChecked()) selectedMaterials.add("металл");

            // Обновляем маркеры на карте
            updateMarkers(selectedMaterials);
        };

        // Привязываем слушатель к чекбоксам
        plasticFilter.setOnCheckedChangeListener(filterChangeListener);
        glassFilter.setOnCheckedChangeListener(filterChangeListener);
        paperFilter.setOnCheckedChangeListener(filterChangeListener);
        metalFilter.setOnCheckedChangeListener(filterChangeListener);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        zoomInButton.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        zoomOutButton.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        toggleFavoritesButton.setOnClickListener(v -> {
            showOnlyFavorites = !showOnlyFavorites;
            Set<String> selectedMaterials = new HashSet<>();
            if (plasticFilter.isChecked()) selectedMaterials.add("пластик");
            if (glassFilter.isChecked()) selectedMaterials.add("стекло");
            if (paperFilter.isChecked()) selectedMaterials.add("бумага");
            if (metalFilter.isChecked()) selectedMaterials.add("металл");
            updateMarkers(selectedMaterials);
        });
    }

    private BitmapDescriptor resizeIcon(int resId, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }

    private void addMarkers(GoogleMap googleMap, List<RecyclingPoint> points) {
        for (RecyclingPoint point : points) {
            // Пример координат, замените их на реальные
            LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
            // Создание пользовательского значка
            BitmapDescriptor customIcon = resizeIcon(R.drawable.leaf_marker, 120, 120);

            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(point.getName())
                    .snippet(point.getDescription())
                    .icon(customIcon));
            Log.d("RecyclingPoints", "" + point.getName());

            // Устанавливаем полные данные в качестве тега
            marker.setTag(point);
        }
    }

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View infoWindowView;

        public CustomInfoWindowAdapter() {
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            infoWindowView = inflater.inflate(R.layout.custom_info_window, null);
        }

        @Nullable
        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            // Настраиваем вид окна
            TextView title = infoWindowView.findViewById(R.id.centre_title);
            if (marker.getTitle().length() > 25) {
                title.setText(marker.getTitle().substring(0, 25) + "...");
            } else {
                title.setText(marker.getTitle());
            }

            // Возвращаем кастомное окно
            return infoWindowView;
        }

        @Nullable
        @Override
        public View getInfoContents(@NonNull Marker marker) {
            return null; // Используем полный вид окна
        }
    }

    private void showBottomSheet(Marker marker) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_bottom_sheet, null);

        RecyclingPoint point = (RecyclingPoint) marker.getTag();
        if (point == null) return;

        TextView title = bottomSheetView.findViewById(R.id.title);
        TextView description = bottomSheetView.findViewById(R.id.description);
        TextView address = bottomSheetView.findViewById(R.id.address);
        TextView wasteTypes = bottomSheetView.findViewById(R.id.waste_types);
        TextView schedule = bottomSheetView.findViewById(R.id.schedule);
        Button addLocationButton = bottomSheetView.findViewById(R.id.add_location_button);

        title.setText(point.getName());
        description.setText(point.getDescription());
        address.setText(point.getAddress());
        String wasteTypesText = TextUtils.join(", ", point.getMaterials());
        wasteTypes.setText(wasteTypesText);
        schedule.setText(point.getFormattedSchedule());

        addLocationButton.setOnClickListener(v -> {
            FavoritesManager favoritesManager = new FavoritesManager(requireContext());
            favoritesManager.addPointToFavorites(point);
            Toast.makeText(requireContext(), "Место добавлено в избранное", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    // Модифицируем метод updateMarkers для отображения избранных пунктов
    public void updateMarkers(Set<String> selectedMaterials) {
        if (mMap == null || viewModel == null) return;

        mMap.clear();

        viewModel.getRecyclingPoints().observe(getViewLifecycleOwner(), points -> {
            if (points != null && !points.isEmpty()) {
                List<RecyclingPoint> filteredPoints = points.stream()
                        .filter(point -> point.getMaterials().containsAll(selectedMaterials))
                        .collect(Collectors.toList());

                if (showOnlyFavorites) {
                    FavoritesManager favoritesManager = new FavoritesManager(requireContext());
                    filteredPoints = favoritesManager.getFavoritePoints();
                }

                addMarkers(mMap, filteredPoints);

                if (filteredPoints.isEmpty()) {
                    Toast.makeText(requireContext(), "Нет пунктов для отображения.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
