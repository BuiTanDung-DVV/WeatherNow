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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WeatherTest";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_MAP_LOCATION = 100;
    private TextView cityText, tempText, descText, humidityText, windText;
    private Spinner citySpinner;
    private Button btnForecast, btnCurrentLocation, btnMap;
    private String selectedCity = "Hanoi"; // Thành phố mặc định


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
        btnMap = findViewById(R.id.btnMapLocation);
        citySpinner = findViewById(R.id.locationSpinner);

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
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnForecast.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForecastActivity.class);
            intent.putExtra("city_name", selectedCity);
            startActivity(intent);
        });

        btnCurrentLocation.setOnClickListener(v -> fetchCurrentLocation());

        btnMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivityForResult(intent, REQUEST_MAP_LOCATION);
        });

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
        Log.d(TAG, "Đang yêu cầu quyền truy cập vị trí...");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        Log.d(TAG, "Đã có quyền truy cập vị trí, lấy vị trí...");
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Log.d(TAG, "Vị trí hiện tại: " + location.getLatitude() + ", " + location.getLongitude());

                        // Tiến hành xử lý tiếp theo
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                String city = address.getAdminArea(); // Sử dụng adminArea (tỉnh/thành phố)

                                // Kiểm tra các thành phần địa chỉ khác
                                String country = address.getCountryName();
                                String postalCode = address.getPostalCode();
                                String subLocality = address.getSubLocality();
                                String featureName = address.getFeatureName();

                                // In ra thông tin địa chỉ để kiểm tra
                                Log.d(TAG, "Địa chỉ: " + address.toString());
                                Log.d(TAG, "Tên thành phố: " + city);
                                Log.d(TAG, "Quốc gia: " + country);
                                Log.d(TAG, "Mã bưu điện: " + postalCode);
                                Log.d(TAG, "Địa phương: " + subLocality);
                                Log.d(TAG, "Tên địa điểm: " + featureName);

                                if (city != null) {
                                    Log.d(TAG, "Thành phố từ GPS: " + city);
                                    selectedCity = city;
                                    fetchWeather(selectedCity); // Gọi API lấy thời tiết cho thành phố
                                    updateCitySpinner(city); // Cập nhật Spinner với thành phố mới
                                } else {
                                    Log.e(TAG, "Không thể lấy tên thành phố từ GPS.");
                                    cityText.setText("Không thể xác định thành phố từ vị trí.");
                                }
                            } else {
                                Log.e(TAG, "Không tìm thấy địa chỉ từ tọa độ.");
                                cityText.setText("Không thể xác định địa chỉ.");
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Lỗi geocoder: " + e.getMessage());
                        }
                    } else {
                        Log.e(TAG, "Vị trí hiện tại không có giá trị.");
                        cityText.setText("Không thể lấy vị trí.");
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Không thể lấy vị trí hiện tại", e);
                    cityText.setText("Không thể lấy vị trí.");
                });
    }

    private void updateCitySpinner(String city) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) citySpinner.getAdapter();
        int position = adapter.getPosition(city);

        if (position == -1) {
            // Thành phố không có trong danh sách, thêm mới vào dữ liệu
            Log.d(TAG, "Thành phố không có trong danh sách, thêm mới: " + city);

            // Thêm thành phố vào danh sách trong Spinner
            addCityToSpinner(city);
        }

        // Chọn thành phố vừa thêm hoặc đã có
        citySpinner.setSelection(adapter.getPosition(city)); // Chọn thành phố mới thêm hoặc đã có trong danh sách
    }

    private void addCityToSpinner(String city) {
        // Lấy danh sách các thành phố từ resources
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) citySpinner.getAdapter();
        List<String> cityList = new ArrayList<>();

        // Đọc dữ liệu từ resources
        String[] cities = getResources().getStringArray(R.array.location_list);
        for (String cityName : cities) {
            cityList.add(cityName);
        }

        // Thêm thành phố mới vào danh sách
        cityList.add(city);

        // Cập nhật lại dữ liệu cho adapter
        ArrayAdapter<String> newAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityList);
        newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(newAdapter);
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
