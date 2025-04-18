package com.example.weathernow;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLatLng;
    private Marker marker;
    private View btnConfirm;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        searchView = findViewById(R.id.searchView);
        btnConfirm = findViewById(R.id.btnConfirmLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Thêm OnClickListener cho SearchView
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false); // Mở rộng thanh tìm kiếm và hiển thị bàn phím
            }
        });
        btnConfirm.setOnClickListener(v -> {
            if (currentLatLng != null) {
                // Lấy tên thành phố từ tọa độ
                String selectedCityName = getCityNameFromLatLng(currentLatLng);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("lat", currentLatLng.latitude);
                resultIntent.putExtra("lng", currentLatLng.longitude);
                resultIntent.putExtra("selected_city", selectedCityName);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(MapActivity.this, "Vui lòng chọn vị trí", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchLocation(String cityName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                currentLatLng = latLng;

                // Update map with the searched location
                if (mMap != null) {
                    mMap.clear();
                    marker = mMap.addMarker(new MarkerOptions().position(latLng).title(cityName));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                }
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error finding location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Kiểm tra quyền truy cập vị trí
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                marker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Vị trí hiện tại"));
            }
        });

        mMap.setOnMapClickListener(latLng -> {
            currentLatLng = latLng;
            if (marker != null) marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Vị trí đã chọn"));
        });
    }

    private String getCityNameFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getLocality() != null ? address.getLocality() : address.getAdminArea();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Không rõ vị trí";
    }
}
