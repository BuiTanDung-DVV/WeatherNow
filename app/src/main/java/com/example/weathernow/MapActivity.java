package com.example.weathernow;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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

import com.example.weathernow.helper.LocaleHelper;
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
    private SearchView searchView;
    @Override
    protected void attachBaseContext(Context newBase) {
        String language = LocaleHelper.getStoredLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //kiêm tra kết nối internet
        if (!MainActivity.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.map_requires_internet), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        searchView = findViewById(R.id.searchView);
        View btnConfirm = findViewById(R.id.btnConfirmLocation);
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

        searchView.setOnClickListener(v -> searchView.setIconified(false));

        btnConfirm.setOnClickListener(v -> {
            if (currentLatLng != null) {
                String selectedCityName = getCityNameFromLatLng(currentLatLng);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("lat", currentLatLng.latitude);
                resultIntent.putExtra("lng", currentLatLng.longitude);
                resultIntent.putExtra("selected_city", selectedCityName);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(MapActivity.this, getString(R.string.please_select_location), Toast.LENGTH_SHORT).show();
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

                if (mMap != null) {
                    mMap.clear();
                    marker = mMap.addMarker(new MarkerOptions().position(latLng).title(cityName));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                }
            } else {
                Toast.makeText(this, getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error_finding_location), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
                marker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title(getString(R.string.current_location1)));
            }
        });

        mMap.setOnMapClickListener(latLng -> {
            currentLatLng = latLng;
            if (marker != null) marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.selected_location)));
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
        return getString(R.string.unknown_location);
    }
    @Override
    protected void onResume() {
        super.onResume();
        String currentLang = LocaleHelper.getStoredLanguage(this);
        LocaleHelper.updateLocale(this, currentLang);
    }
}