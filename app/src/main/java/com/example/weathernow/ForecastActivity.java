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

    private TextView day1Forecast, day2Forecast, day3Forecast, day4Forecast, day5Forecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        day1Forecast = findViewById(R.id.day1Forecast);
        day2Forecast = findViewById(R.id.day2Forecast);
        day3Forecast = findViewById(R.id.day3Forecast);
        day4Forecast = findViewById(R.id.day4Forecast);
        day5Forecast = findViewById(R.id.day5Forecast);

        Retrofit retrofit = ApiClient.getClient(this);
        WeatherService service = retrofit.create(WeatherService.class);

        Call<JsonObject> call = service.getForecastByCity("Hanoi", "metric", "vi");

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject data = response.body();
                    JsonArray list = data.getAsJsonArray("list");

                    // Sử dụng LinkedHashMap để giữ thứ tự ngày
                    Map<String, String> dailyForecast = new LinkedHashMap<>();

                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd/MM", new Locale("vi"));

                    for (int i = 0; i < list.size(); i++) {
                        JsonObject item = list.get(i).getAsJsonObject();
                        String dt_txt = item.get("dt_txt").getAsString();

                        try {
                            Date date = inputFormat.parse(dt_txt);
                            String day = outputFormat.format(date);

                            // Chỉ lấy bản tin dự báo vào 12:00 mỗi ngày
                            if (dt_txt.contains("12:00:00") && !dailyForecast.containsKey(day)) {
                                double temp = item.getAsJsonObject("main").get("temp").getAsDouble();
                                String desc = item.getAsJsonArray("weather")
                                        .get(0).getAsJsonObject()
                                        .get("description").getAsString();

                                String forecast = day + ": " + temp + "°C - " + desc;
                                dailyForecast.put(day, forecast);

                                // Dừng sau 5 ngày
                                if (dailyForecast.size() == 5) break;
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi định dạng ngày: " + e.getMessage());
                        }
                    }

                    // Gán dữ liệu cho từng TextView
                    int index = 0;
                    for (String forecast : dailyForecast.values()) {
                        switch (index) {
                            case 0:
                                day1Forecast.setText(forecast);
                                break;
                            case 1:
                                day2Forecast.setText(forecast);
                                break;
                            case 2:
                                day3Forecast.setText(forecast);
                                break;
                            case 3:
                                day4Forecast.setText(forecast);
                                break;
                            case 4:
                                day5Forecast.setText(forecast);
                                break;
                        }
                        index++;
                    }

                } else {
                    day1Forecast.setText("Lỗi phản hồi: " + response.code());
                    Log.e(TAG, "Lỗi phản hồi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                day1Forecast.setText("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage(), t);
            }
        });
    }
}
