package com.example.weathernow;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.weathernow.api.ApiClient;
import com.example.weathernow.api.WeatherService;
import com.example.weathernow.data.AppDatabase;
import com.example.weathernow.data.WeatherEntity;
import com.example.weathernow.firebase.AuthManager;
import com.example.weathernow.firebase.FirestoreManager;
import com.example.weathernow.helper.LocaleHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1; // Mã yêu cầu quyền
    private WeatherService weatherService;
    private AppDatabase db;
    private AuthManager authManager;
    private FirestoreManager firestore;
    private TextView cityTextView, tempTextView;
    private String weatherApiKey;
    private ExecutorService executorService;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(newBase);
        LocaleHelper.applySavedLocale(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.applySavedLocale(this);
        setContentView(R.layout.activity_main);

        // Initialize views
        cityTextView = findViewById(R.id.cityTextView);
        tempTextView = findViewById(R.id.tempTextView);

        // Initialize services
        weatherService = ApiClient.getClient().create(WeatherService.class);
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "weather_db").build();
        authManager = new AuthManager();
        firestore = new FirestoreManager();
        executorService = Executors.newSingleThreadExecutor();

        // Enable Firestore offline persistence
        enableFirestoreOfflinePersistence();

        // Load API key from assets
        loadApiKey();

        // Sign in anonymously with Firebase
        authManager.signInAnonymously(task -> {
            if (task.isSuccessful()) {
                loadWeather("Hanoi", LocaleHelper.getSavedLanguage(this));
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableFirestoreOfflinePersistence() {
        FirebaseFirestore firestoreInstance = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)  // Enable offline persistence
                .build();
        firestoreInstance.setFirestoreSettings(settings);
    }

    private void loadApiKey() {
        try {
            InputStream inputStream = getAssets().open("weather-api-key.json");
            JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
            weatherApiKey = json.get("API_Key").getAsString();
        } catch (Exception e) {
            handleApiKeyError(e);
        }
    }

    private void handleApiKeyError(Exception e) {
        Log.e("API_KEY_ERROR", "Failed to load API key", e);
        Toast.makeText(this, "Failed to load API key", Toast.LENGTH_SHORT).show();
        weatherApiKey = "";
    }

    private void loadWeather(String city, String lang) {
        if (weatherApiKey.isEmpty()) {
            Toast.makeText(this, "API key is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if weather data for the city already exists in Room
        LiveData<WeatherEntity> weatherLiveData = db.weatherDao().getWeatherByCity(city);
        weatherLiveData.observe(this, new Observer<WeatherEntity>() {
            @Override
            public void onChanged(WeatherEntity weather) {
                if (weather != null) {
                    // If data exists, display it
                    cityTextView.setText(city);
                    tempTextView.setText(weather.temperature + "°C");
                } else {
                    // If no data, fetch from the API
                    fetchWeatherFromApi(city, lang);
                }
            }
        });
    }

    private void fetchWeatherFromApi(String city, String lang) {
        // Make API call to fetch weather data
        Call<JsonObject> call = weatherService.getWeatherByCity(city, weatherApiKey, "metric", lang);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject obj = response.body();
                    double temp = obj.getAsJsonObject("main").get("temp").getAsDouble();

                    // Display the weather information
                    cityTextView.setText(city);
                    tempTextView.setText(temp + "°C");

                    // Create WeatherEntity object
                    WeatherEntity weather = new WeatherEntity();
                    weather.city = city;
                    weather.temperature = temp;
                    weather.timestamp = System.currentTimeMillis();

                    // Save data to Room and Firestore
                    executorService.execute(() -> {
                        db.weatherDao().insert(weather);
                        FirebaseUser user = authManager.getCurrentUser();
                        if (user != null) {
                            firestore.saveWeatherData(user.getUid(), weather);
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "API error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                handleApiFailure(t);
            }
        });
    }

    private void handleApiFailure(Throwable t) {
        Log.e("API_ERROR", t.getMessage(), t);
        Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }
    // Phương thức yêu cầu quyền thông báo
    public void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Nếu quyền được cấp, tiếp tục thực hiện công việc của WeatherWorker
            } else {
                Toast.makeText(this, "Quyền thông báo bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

