package com.example.weathernow;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weathernow.api.ApiClient;
import com.example.weathernow.api.WeatherService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ForecastActivity extends AppCompatActivity {

    private static final String TAG = "ForecastActivity";

    private TextView[] forecastViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        forecastViews = new TextView[]{
                findViewById(R.id.day1Forecast),
                findViewById(R.id.day2Forecast),
                findViewById(R.id.day3Forecast),
                findViewById(R.id.day4Forecast),
                findViewById(R.id.day5Forecast)
        };

        String cityName = getIntent().getStringExtra("city_name"); // Sửa tên key cho đúng
        if (cityName == null || cityName.isEmpty()) {
            cityName = "Hanoi"; // fallback mặc định
        }

        fetchForecast(cityName);
    }

    private void fetchForecast(String cityName) {
        Retrofit retrofit = ApiClient.getClient(this);
        WeatherService service = retrofit.create(WeatherService.class);

        service.getForecastByCity(cityName, "metric", "vi").enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    displayError("Lỗi phản hồi: " + response.code());
                    return;
                }

                JsonArray list = response.body().getAsJsonArray("list");
                if (list == null) {
                    displayError("Không có dữ liệu dự báo.");
                    return;
                }

                Map<String, String> dailyForecast = parseForecastData(list);

                int i = 0;
                for (String forecast : dailyForecast.values()) {
                    if (i < forecastViews.length) {
                        forecastViews[i++].setText(forecast);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                displayError("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "Lỗi kết nối API", t);
            }
        });
    }

    private Map<String, String> parseForecastData(JsonArray list) {
        Map<String, String> dailyForecast = new LinkedHashMap<>();

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd/MM", new Locale("vi"));

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

                dailyForecast.put(day, day + ": " + temp + "°C - " + desc);

                if (dailyForecast.size() == 5) break;

            } catch (Exception e) {
                Log.e(TAG, "Lỗi định dạng hoặc phân tích dữ liệu: " + e.getMessage(), e);
            }
        }

        return dailyForecast;
    }

    private void displayError(String message) {
        for (TextView view : forecastViews) {
            view.setText(message);
        }
    }
}