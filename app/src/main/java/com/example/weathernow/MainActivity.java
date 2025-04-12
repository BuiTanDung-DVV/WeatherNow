package com.example.weathernow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.weathernow.api.ApiClient;
import com.example.weathernow.api.WeatherService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WeatherTest";
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private TextView cityText, tempText, descText, humidityText, windText;
    private Spinner citySpinner;
    private Button btnForecast, btnCurrentLocation;
    private String selectedCity = "Hanoi";

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityText = findViewById(R.id.cityText);
        tempText = findViewById(R.id.tempText);
        descText = findViewById(R.id.descText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        btnForecast = findViewById(R.id.btnForecast);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        citySpinner = findViewById(R.id.locationSpinner);

        // Adapter cho Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.location_list,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String city = parent.getItemAtPosition(position).toString();
                if (!city.equals(selectedCity)) {
                    selectedCity = city;
                    fetchWeather(selectedCity);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không xử lý
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnForecast.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForecastActivity.class);
            intent.putExtra("city_name", selectedCity);
            startActivity(intent);
        });

        btnCurrentLocation.setOnClickListener(v -> fetchCurrentLocation());

        // Hiển thị thời tiết ban đầu
        fetchWeather(selectedCity);
    }

    private void fetchWeather(String cityName) {
        Retrofit retrofit = ApiClient.getClient(this);
        WeatherService service = retrofit.create(WeatherService.class);

        Call<JsonObject> call = service.getWeatherByCity(cityName, "metric", "vi");

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject data = response.body();
                    Log.d(TAG, "Dữ liệu thời tiết: " + data.toString());

                    String city = data.get("name").getAsString();
                    JsonObject main = data.getAsJsonObject("main");
                    double temp = main.get("temp").getAsDouble();
                    int humidity = main.get("humidity").getAsInt();
                    JsonObject wind = data.getAsJsonObject("wind");
                    double windSpeed = wind.get("speed").getAsDouble();
                    JsonArray weatherArray = data.getAsJsonArray("weather");
                    String description = "";
                    if (weatherArray.size() > 0) {
                        JsonObject weather = weatherArray.get(0).getAsJsonObject();
                        description = weather.get("description").getAsString();
                    }

                    cityText.setText("Thành phố: " + city);
                    tempText.setText("Nhiệt độ: " + temp + "°C");
                    descText.setText("Trạng thái: " + description);
                    humidityText.setText("Độ ẩm: " + humidity + "%");
                    windText.setText("Gió: " + windSpeed + " m/s");
                } else {
                    cityText.setText("Lỗi phản hồi: " + response.code());
                    Log.e(TAG, "Lỗi phản hồi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                cityText.setText("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage(), t);
            }
        });
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                String city = addresses.get(0).getLocality();
                                if (city != null) {
                                    selectedCity = city;

                                    // Tìm thành phố trong danh sách spinner
                                    ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) citySpinner.getAdapter();
                                    int position = adapter.getPosition(city);

                                    if (position >= 0) {
                                        citySpinner.setSelection(position);
                                    } else {
                                        // Nếu không có trong danh sách thì vẫn lấy thời tiết
                                        fetchWeather(selectedCity);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Lỗi geocoder: " + e.getMessage());
                        }
                    }
                })
                .addOnFailureListener(this, e -> Log.e(TAG, "Không thể lấy vị trí hiện tại", e));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            }
        }
    }
}
