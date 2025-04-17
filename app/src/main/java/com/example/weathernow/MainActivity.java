package com.example.weathernow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.weathernow.api.ApiClient;
import com.example.weathernow.api.WeatherService;
import com.example.weathernow.data.AppDatabase;
import com.example.weathernow.data.WeatherDao;
import com.example.weathernow.data.WeatherEntity;
import com.example.weathernow.firebase.FirestoreManager;
import com.example.weathernow.helper.WeatherShareHelper;
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
    private Button btnForecast, btnCurrentLocation;

    private ImageButton btnMap;

    private String selectedCity = "Hanoi";
    private List<String> cityList = new ArrayList<>();
    private AppDatabase appDatabase;
    private FirestoreManager firestoreManager;
    ImageButton btnShareWeather;

    private FusedLocationProviderClient fusedLocationClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appDatabase = AppDatabase.getInstance(getApplicationContext());
        firestoreManager = new FirestoreManager();

/*        cityText = findViewById(R.id.cityText);*/
        tempText = findViewById(R.id.tempText);
        descText = findViewById(R.id.descText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        btnForecast = findViewById(R.id.btnForecast);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        btnMap = findViewById(R.id.btnMapLocation);
        citySpinner = findViewById(R.id.locationSpinner);

        loadCityList();

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

        // Đồng bộ dữ liệu từ Firestore vào Room
        firestoreManager.syncWeatherDataFromCloud(appDatabase.weatherDao());

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

        btnShareWeather = findViewById(R.id.btnShareWeather);

        btnShareWeather.setOnClickListener(v -> {
            new Thread(() -> {
                WeatherEntity latestWeather = appDatabase.weatherDao().getLatestWeatherByCity(selectedCity);
                runOnUiThread(() -> {
                    if (latestWeather != null) {
                        WeatherShareHelper.shareWeatherCard(MainActivity.this, latestWeather);
                    } else {
                        Log.e(TAG, "Không có dữ liệu thời tiết để chia sẻ.");
                        cityText.setText("Không có dữ liệu thời tiết để chia sẻ.");
                    }
                });
            }).start();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MAP_LOCATION && resultCode == RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("lat", 0);
            double lng = data.getDoubleExtra("lng", 0);
            String selectedCityFromMap = data.getStringExtra("selected_city");

            if (selectedCityFromMap != null && !selectedCityFromMap.isEmpty()) {
                selectedCity = selectedCityFromMap;
                if (!cityList.contains(selectedCity)) {
                    cityList.add(selectedCity);
                }
                updateCitySpinner(cityList);
                fetchWeather(selectedCity);
            }  else {
                // Nếu không có tên thành phố, sử dụng reverse geocoding để xác định thành phố
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String city = address.getAdminArea();
                        if (city != null) {
                            selectedCity = city;
                            if (!cityList.contains(selectedCity)) {
                                cityList.add(selectedCity);
                            }
                            updateCitySpinner(cityList);
                            fetchWeather(selectedCity);
                        } else {
                            cityText.setText("Không thể xác định thành phố từ vị trí bản đồ.");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    cityText.setText("Lỗi geocoder.");
                }
            }
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void loadCityList() {
        if (isNetworkAvailable()) {
            // Tải danh sách từ Firebase
            firestoreManager.getCityList(cityNames -> runOnUiThread(() -> {
                if (!cityNames.isEmpty()) {
                    cityList.clear();
                    cityList.addAll(cityNames);
                    updateCitySpinner(cityList);
                }
            }));
        } else {
            // Tải danh sách từ Room
            new Thread(() -> {
                List<WeatherEntity> weatherEntities = appDatabase.weatherDao().getAll();
                List<String> cityNames = new ArrayList<>();
                for (WeatherEntity entity : weatherEntities) {
                    if (!cityNames.contains(entity.getCity())) {
                        cityNames.add(entity.getCity());
                    }
                }
                runOnUiThread(() -> {
                    cityList.clear();
                    cityList.addAll(cityNames);
                    updateCitySpinner(cityList);
                });
            }).start();
        }
    }
    private void updateCitySpinner(List<String> cityNames) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    @NonNull
    private String standardizeCityName(@NonNull String city) {
        if (city.startsWith("Thành phố ")) {
            return city.replace("Thành phố ", "").trim();
        }
        return city;
    }
    private void fetchWeather(String cityName) {
        String standardizedCity = standardizeCityName(cityName);
        Retrofit retrofit = ApiClient.getClient(this);
        WeatherService service = retrofit.create(WeatherService.class);

        Call<JsonObject> call = service.getWeatherByCity(standardizedCity, "metric", "vi");

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    Log.d(TAG, "Dữ liệu thời tiết: " + data.toString());

                    try {
                        String city = data.get("name").getAsString();
                        JsonObject main = data.getAsJsonObject("main");
                        double temp = main.get("temp").getAsDouble();
                        int humidity = main.get("humidity").getAsInt();
                        JsonObject wind = data.getAsJsonObject("wind");
                        double windSpeed = wind.get("speed").getAsDouble();
                        JsonObject coord = data.getAsJsonObject("coord");
                        double latitude = coord.get("lat").getAsDouble();
                        double longitude = coord.get("lon").getAsDouble();
                        long timestamp = System.currentTimeMillis();

                        JsonArray weatherArray = data.getAsJsonArray("weather");
                        String description = weatherArray.get(0).getAsJsonObject().get("description").getAsString();

                        WeatherEntity weatherEntity = new WeatherEntity();
                        weatherEntity.setCity(city);
                        weatherEntity.setTemperature(temp);
                        weatherEntity.setDescription(description);
                        weatherEntity.setHumidity(humidity);
                        weatherEntity.setWindSpeed(windSpeed);
                        weatherEntity.setLatitude(latitude);
                        weatherEntity.setLongitude(longitude);
                        weatherEntity.setTimestamp(timestamp);

                        // Lưu vào Room
                        new Thread(() -> {
                            appDatabase.weatherDao().insertWeather(weatherEntity);

                            runOnUiThread(() -> {
/*                                cityText.setText("Thành phố: " + city);*/
                                tempText.setText("Nhiệt độ: " + temp + "°C");
                                descText.setText("Trạng thái: " + description);
                                humidityText.setText("Độ ẩm: " + humidity + "%");
                                windText.setText("Gió: " + windSpeed + " m/s");
                            });
                        }).start();
                        if (!cityList.contains(city)) {
                            cityList.add(city);
                            updateCitySpinner(cityList);
                        }
                        int cityIndex = cityList.indexOf(city);
                        citySpinner.setSelection(cityIndex);

                        WeatherDao weatherDao = appDatabase.weatherDao();
                        // Lưu vào Firebase Firestore
                        firestoreManager.saveWeatherData(weatherEntity, weatherDao);

                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi phân tích JSON: " + e.getMessage(), e);
                        runOnUiThread(() -> cityText.setText("Lỗi phân tích dữ liệu thời tiết."));
                    }

                } else {
                    Log.e(TAG, "Lỗi phản hồi: " + response.code());
                    runOnUiThread(() -> cityText.setText("Không tìm thấy thành phố hoặc phản hồi lỗi: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage(), t);
                runOnUiThread(() -> cityText.setText("Không thể kết nối đến máy chủ: " + t.getMessage()));
            }
        });

        Log.d("WeatherTest", "Đang lấy dữ liệu thời tiết cho: " + standardizedCity);
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
                                    updateCitySpinner(List.of(city)); // Cập nhật Spinner với thành phố mới
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Log.e(TAG, "Location permission denied.");
                cityText.setText("Permission denied. Cannot fetch location.");
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - selectedCity: " + selectedCity);

        if (selectedCity != null && !selectedCity.isEmpty()) {
            fetchWeather(selectedCity);
            updateCitySpinner(cityList);
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart - selectedCity: " + selectedCity);
    }

}