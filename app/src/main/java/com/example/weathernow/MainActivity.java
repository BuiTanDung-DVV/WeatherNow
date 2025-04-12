package com.example.weathernow;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weathernow.api.ApiClient;
import com.example.weathernow.api.WeatherService;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WeatherTest";
    private TextView weatherText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // ⚠️ Thiết lập layout ở đây

        weatherText = findViewById(R.id.weatherText); // ⚠️ Liên kết TextView trong layout

        // Gọi API để lấy thời tiết theo thành phố
        Retrofit retrofit = ApiClient.getClient(this);
        WeatherService service = retrofit.create(WeatherService.class);

        Call<JsonObject> call = service.getWeatherByCity("Hanoi", "metric", "vi");

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject data = response.body();
                    weatherText.setText(data.toString()); // 🟢 Hiển thị kết quả trên giao diện
                    Log.d(TAG, "Dữ liệu thời tiết: " + data.toString());
                } else {
                    weatherText.setText("Lỗi phản hồi: " + response.code());
                    Log.e(TAG, "Lỗi phản hồi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                weatherText.setText("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage(), t);
            }
        });
    }
}
