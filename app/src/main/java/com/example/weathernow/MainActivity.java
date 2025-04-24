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
import android.widget.ImageView;
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
import com.example.weathernow.helper.LocaleHelper;
import com.example.weathernow.helper.WeatherShareHelper;
import com.example.weathernow.helper.WeatherUtils;
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

    private static final String TAG = "WeatherNow";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_MAP_LOCATION = 100;
    private TextView cityText, tempText, descText, humidityText, windText;
    private Spinner citySpinner;
    private String selectedCity = "Hanoi"; // Thành phố mặc định
    private final List<String> cityList = new ArrayList<>();
    private AppDatabase appDatabase;
    private FirestoreManager firestoreManager;
    ImageButton btnShareWeather,btnMap, btnSettings;

    private FusedLocationProviderClient fusedLocationClient;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getStoredLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appDatabase = AppDatabase.getInstance(getApplicationContext());
        firestoreManager = new FirestoreManager();

        cityText = findViewById(R.id.cityText);
        tempText = findViewById(R.id.tempText);
        descText = findViewById(R.id.descText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        Button btnForecast = findViewById(R.id.btnForecast);
        Button btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        btnMap = findViewById(R.id.btnMapLocation);
        citySpinner = findViewById(R.id.locationSpinner);
        btnSettings = findViewById(R.id.btnSettings);

        loadCityList();

        ImageView notificationIcon = findViewById(R.id.imageView2);
        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String city = parent.getItemAtPosition(position).toString();
                if (!city.equals(selectedCity)) {
                    selectedCity = city;
                    cityText.setText(selectedCity);
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

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);

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
                        Log.e(TAG, getString(R.string.no_weather_data_to_share2));
                        cityText.setText(getString(R.string.no_weather_data_to_share2));
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
                            cityText.setText(getString(R.string.cannot_get_city_from_map2));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    cityText.setText(getString(R.string.geocoder_error2));
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
        if (cityNames == null || cityNames.isEmpty()) {
            Log.e(TAG, "City list is empty. Cannot update spinner.");
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, cityNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        citySpinner.setAdapter(adapter);

        if (cityNames.contains(selectedCity)) {
            int cityIndex = cityNames.indexOf(selectedCity);
            citySpinner.setSelection(cityIndex);
        } else {
            selectedCity = cityNames.get(0);
            cityText.setText(selectedCity);
            citySpinner.setSelection(0);
        }
    }
    @NonNull
    private String standardizeCityName(@NonNull String city) {
        if (city.startsWith(getString(R.string.city3))) {
            return city.replace(getString(R.string.city3), "").trim();
        }
        return city;
    }
    private void fetchWeather(String cityName) {
        String standardizedCity = standardizeCityName(cityName);
        Retrofit retrofit = ApiClient.getClient(this);
        //set ngôn ngữ
        WeatherService service = retrofit.create(WeatherService.class);
        String languageCode = LocaleHelper.getStoredLanguage(this);
        Call<JsonObject> call = service.getWeatherByCity(standardizedCity, "metric", languageCode);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    Log.d(TAG, getString(R.string.weather_data3) + data.toString());

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
                        if (description != null && !description.isEmpty()) {
                            description = description.substring(0, 1).toUpperCase() + description.substring(1);
                        }

                        String standardizedDescription = WeatherUtils.mapLocalizedDescription(description);
                        int weatherIcon = WeatherUtils.getWeatherIcon(standardizedDescription);

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
                        assert description != null;
                        String finalDescription = description.toLowerCase();

                        new Thread(() -> {
                            appDatabase.weatherDao().insertWeather(weatherEntity);

                            runOnUiThread(() -> {
                                cityText.setText(getString(R.string.share_city_placeholder) + ": " + city);
                                tempText.setText(Math.round(temp) + "°C");
                                descText.setText(finalDescription);
                                humidityText.setText(String.format(getString(R.string.share_humidity_format), humidity));
                                windText.setText(String.format(getString(R.string.share_wind_format), windSpeed));

                                ImageView weatherImageView = findViewById(R.id.imageView4);
                                weatherImageView.setImageResource(weatherIcon);
                                NotificationActivity.createWeatherNotification(MainActivity.this, weatherEntity);
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
                        cityText.setText(selectedCity);
                    } catch (Exception e) {
                        Log.e(TAG, getString(R.string.json_parse_error3) + e.getMessage(), e);
                        runOnUiThread(() -> cityText.setText(getString(R.string.parsing_weather_error2)));
                    }

                } else {
                    Log.e(TAG, getString(R.string.response_error3) + response.code());
                    runOnUiThread(() -> cityText.setText(getString(R.string.not_found_or_error_response2) + response.code()));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, getString(R.string.connection_error3) + t.getMessage(), t);
                runOnUiThread(() -> cityText.setText(getString(R.string.server_connection_failed2) + t.getMessage()));
            }
        });

        Log.d("WeatherTest", getString(R.string.fetching_weather_data3) + standardizedCity);
    }
    private void fetchCurrentLocation() {
        Log.d(TAG, getString(R.string.requesting_location_permission2));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        Log.d(TAG, getString(R.string.location_permission_granted2));
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Log.d(TAG, getString(R.string.current_location2) + location.getLatitude() + ", " + location.getLongitude());

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
                                Log.d(TAG, getString(R.string.address3) + address.toString());
                                Log.d(TAG, getString(R.string.city_name3) + city);
                                Log.d(TAG, getString(R.string.country3) + country);
                                Log.d(TAG, getString(R.string.postal_code3) + postalCode);
                                Log.d(TAG, getString(R.string.locality3) + subLocality);
                                Log.d(TAG, getString(R.string.feature_name3) + featureName);

                                if (city != null) {
                                    Log.d(TAG, getString(R.string.city_from_gps3) + city);
                                    selectedCity = city;
                                    fetchWeather(selectedCity); // Gọi API lấy thời tiết cho thành phố
                                    if (!cityList.contains(city)) {
                                        cityList.add(city);
                                    }
                                    cityText.setText(selectedCity);
                                    updateCitySpinner(cityList);// Cập nhật Spinner với thành phố mới
                                } else {
                                    Log.e(TAG, getString(R.string.cannot_get_city_from_gps3));
                                    cityText.setText(getString(R.string.cannot_get_city_from_location3));
                                }
                            } else {
                                    Log.e(TAG, getString(R.string.cannot_get_address_from_coordinates3));
                                cityText.setText(getString(R.string.cannot_determine_address3));
                            }
                        } catch (IOException e) {
                            Log.e(TAG, getString(R.string.geocoder_error3) + e.getMessage());
                        }
                    } else {
                        Log.e(TAG, getString(R.string.location_not_available3));
                        cityText.setText(getString(R.string.cannot_get_location3));
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, getString(R.string.cannot_get_current_location3), e);
                    cityText.setText(getString(R.string.cannot_get_location3));
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
        // Cập nhật ngôn ngữ qua LocaleHelper
        String currentLang = LocaleHelper.getStoredLanguage(this);
        LocaleHelper.updateLocale(this, currentLang);

        cityText.setText(getString(R.string.share_city_placeholder));
        tempText.setText(getString(R.string.temperature_placeholder));
        descText.setText(getString(R.string.share_desc_placeholder));
        humidityText.setText(getString(R.string.share_humidity_format).replace("%1$d", "--"));
        windText.setText(getString(R.string.share_wind_format).replace("%1$.2f", "--"));

        Log.d(TAG, "onResume - selectedCity: " + selectedCity);
        if (selectedCity != null && !selectedCity.isEmpty()) {
            fetchWeather(selectedCity);
            updateCitySpinner(cityList);
        }
    }

}
