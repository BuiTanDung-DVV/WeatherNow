package com.example.weathernow;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weathernow.api.ApiClient;
import com.example.weathernow.api.WeatherService;
import com.example.weathernow.data.AppDatabase;
import com.example.weathernow.data.WeatherEntity;
import com.example.weathernow.helper.LocaleHelper;
import com.example.weathernow.helper.WeatherUtils;
import com.example.weathernow.model.HourlyForecast;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ForecastActivity extends AppCompatActivity {

    private static final String TAG = "ForecastActivity";

    private TextView[] forecastViews;
    private ImageView[] forecastIcons;
    private LinearLayout hourlyForecastContainer;
    @Override
    protected void attachBaseContext(Context newBase) {
        // Lấy ngôn ngữ đã lưu và áp dụng
        String language = LocaleHelper.getStoredLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        hourlyForecastContainer = findViewById(R.id.hourlyForecastContainer);

        ImageButton btnBack = findViewById(R.id.imageView);
        btnBack.setOnClickListener(v -> finish());

        forecastViews = new TextView[]{
                findViewById(R.id.day1Forecast),
                findViewById(R.id.day2Forecast),
                findViewById(R.id.day3Forecast),
                findViewById(R.id.day4Forecast),
                findViewById(R.id.day5Forecast)
        };

        forecastIcons = new ImageView[]{
                findViewById(R.id.day1Icon),
                findViewById(R.id.day2Icon),
                findViewById(R.id.day3Icon),
                findViewById(R.id.day4Icon),
                findViewById(R.id.day5Icon)
        };

        String cityName = getIntent().getStringExtra("city_name");
        if (cityName == null || cityName.isEmpty()) {
            cityName = "Hanoi";
        }

        fetchForecast(cityName);
    }
    private void fetchForecast(String cityName) {
        if (MainActivity.isNetworkAvailable(this)) {
            // Online - fetch from API
            fetchForecastFromApi(cityName);
        } else {
            // Offline - hiên thị dữ liệu đã lưu
            Toast.makeText(this, getString(R.string.offline_mode_forecast), Toast.LENGTH_SHORT).show();
            displayOfflineForecast(cityName);
        }
    }

    private void fetchForecastFromApi(String cityName) {
        Retrofit retrofit = ApiClient.getClient(this);
        WeatherService service = retrofit.create(WeatherService.class);
        String languageCode = LocaleHelper.getStoredLanguage(this);
        service.getForecastByCity(cityName, "metric", languageCode).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    displayError(getString(R.string.response_error3) + response.code());
                    return;
                }

                JsonArray list = response.body().getAsJsonArray("list");
                if (list == null) {
                    displayError(getString(R.string.no_weather_data));
                    return;
                }

                Map<String, String> dailyForecast = parseForecastData(list);
                List<HourlyForecast> hourlyForecasts = parseHourlyForecastData(list);

                int i = 0;
                for (String forecast : dailyForecast.values()) {
                    if (i < forecastViews.length) {
                        forecastViews[i++].setText(forecast);
                    }
                }

                updateHourlyForecast(hourlyForecasts);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                displayError(getString(R.string.connection_error3) + t.getMessage());
                Log.e(TAG,  getString(R.string.connection_error3) +"API", t);
            }
        });
    }
    private void displayOfflineForecast(String cityName) {
        new Thread(() -> {
            WeatherEntity latestWeather = AppDatabase.getInstance(this).weatherDao().getLatestWeatherByCity(cityName);

            runOnUiThread(() -> {
                if (latestWeather != null) {
                    // Display at least today's forecast from cached data
                    String forecast = "Today: " + latestWeather.getTemperature() + "°C - " +
                            latestWeather.getDescription();
                    forecastViews[0].setText(forecast);

                    // Set today's icon
                    int iconResource = WeatherUtils.getWeatherIcon(latestWeather.getDescription());
                    forecastIcons[0].setImageResource(iconResource);

                    // Display message for other days
                    for (int i = 1; i < forecastViews.length; i++) {
                        forecastViews[i].setText(getString(R.string.forecast_data_unavailable_offline));
                    }
                } else {
                    displayError(getString(R.string.no_cached_forecast_data));
                }
            });
        }).start();
    }

    private Map<String, String> parseForecastData(JsonArray list) {
        Map<String, String> dailyForecast = new LinkedHashMap<>();
        int iconIndex = 0;
        // Lấy ngôn ngữ đã lưu từ SharedPreferences
        String languageCode = LocaleHelper.getStoredLanguage(this);
        Locale currentLocale = new Locale(languageCode);
        // Đặt định dạng ngày tháng theo ngôn ngữ đã lưu
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE", currentLocale);

        for (int i = 0; i < list.size(); i++) {
            try {
                JsonObject item = list.get(i).getAsJsonObject();
                String dt_txt = item.get("dt_txt").getAsString();

                if (!dt_txt.contains("12:00:00")) continue;

                Date date = inputFormat.parse(dt_txt);
                String day = outputFormat.format(date);

                if (dailyForecast.containsKey(day)) continue;

                JsonObject main = item.getAsJsonObject("main");
                double temp = main.get("temp").getAsDouble();

                JsonObject weather = item.getAsJsonArray("weather").get(0).getAsJsonObject();
                String desc = weather.get("description").getAsString();
                String icon = weather.get("icon").getAsString();

                dailyForecast.put(day, day + ": " + temp + "°C - " + desc);

                if (iconIndex < forecastIcons.length) {
                    int iconResource = getIconResourceForCode(icon);
                    final int index = iconIndex;
                    runOnUiThread(() -> forecastIcons[index].setImageResource(iconResource));
                    iconIndex++;
                }

                if (dailyForecast.size() == 5) break;

            } catch (Exception e) {
                Log.e(TAG, getString(R.string.hourly_data_parse_error3) + e.getMessage(), e);
            }
        }

        return dailyForecast;
    }

    private int getIconResourceForCode(String iconCode) {
        switch (iconCode) {
            case "01d": return R.drawable.ic_clear;
            case "01n": return R.drawable.ic_fullmoon;
            case "02d": return R.drawable.ic_sunny;
            case "02n": return R.drawable.ic_night;
            case "03d": return R.drawable.ic_cloudy;
            case "03n": return R.drawable.ic_cloudy;
            case "04d": return R.drawable.ic_cloudy;
            case "04n": return R.drawable.ic_night_cloudy;
            case "09d": return R.drawable.ic_rainy;
            case "09n": return R.drawable.ic_night_rain;
            case "10d": return R.drawable.ic_cloudy_rainy;
            case "10n": return R.drawable.ic_night_rain;
            case "11d": return R.drawable.ic_thunder;
            case "11n": return R.drawable.ic_thunder;
            case "13d": return R.drawable.ic_cloudy;
            case "13n": return R.drawable.ic_cloudy;
            case "50d": return R.drawable.ic_cloudy;
            case "50n": return R.drawable.ic_cloudy;
            default: return R.drawable.ic_cloudy;
        }
    }

    private List<HourlyForecast> parseHourlyForecastData(JsonArray list) {
        List<HourlyForecast> hourlyForecasts = new ArrayList<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (int i = 0; i < list.size(); i++) {
            try {
                JsonObject item = list.get(i).getAsJsonObject();
                String dt_txt = item.get("dt_txt").getAsString();

                Date date = inputFormat.parse(dt_txt);
                String time = outputFormat.format(date);

                JsonObject main = item.getAsJsonObject("main");
                double temp = main.get("temp").getAsDouble();

                JsonObject weather = item.getAsJsonArray("weather").get(0).getAsJsonObject();
                String icon = weather.get("icon").getAsString();
                String description = weather.get("description").getAsString();

                HourlyForecast forecast = new HourlyForecast(time, temp, icon, description);
                hourlyForecasts.add(forecast);

                if (hourlyForecasts.size() >= 24) break;

            } catch (Exception e) {
                Log.e(TAG, getString(R.string.data_format_or_parse_error) + e.getMessage(), e);
            }
        }
        return hourlyForecasts;
    }

    private void updateHourlyForecast(List<HourlyForecast> hourlyForecasts) {
        hourlyForecastContainer.removeAllViews();

        for (HourlyForecast forecast : hourlyForecasts) {
            LinearLayout itemView = (LinearLayout) getLayoutInflater().inflate(
                    R.layout.hourly_forecast_item, hourlyForecastContainer, false);

            TextView timeView = itemView.findViewById(R.id.timeText);
            TextView tempView = itemView.findViewById(R.id.tempText);
            ImageView iconView = itemView.findViewById(R.id.weatherIcon);

            timeView.setText(forecast.getTime());
            tempView.setText(String.format("%.1f°C", forecast.getTemperature()));

            iconView.setImageResource(forecast.getIconResourceId());

            hourlyForecastContainer.addView(itemView);
        }
    }

    private void displayError(String message) {
        for (TextView view : forecastViews) {
            view.setText(message);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra và cập nhật ngôn ngữ
        String currentLang = LocaleHelper.getStoredLanguage(this);
        LocaleHelper.updateLocale(this, currentLang);
    }
}