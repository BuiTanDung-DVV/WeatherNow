package com.example.weathernow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weathernow.api.ApiClient;
import com.example.weathernow.api.WeatherService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WeatherTest";

    private TextView cityText, tempText, descText, humidityText, windText;
    private Button btnForecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityText = findViewById(R.id.cityText);
        tempText = findViewById(R.id.tempText);
        descText = findViewById(R.id.descText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        btnForecast = findViewById(R.id.btnForecast); // 👉 Thêm nút

        btnForecast.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForecastActivity.class);
            startActivity(intent);
        });

        Retrofit retrofit = ApiClient.getClient(this);
        WeatherService service = retrofit.create(WeatherService.class);

        Call<JsonObject> call = service.getWeatherByCity("Hanoi", "metric", "vi");

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
}
