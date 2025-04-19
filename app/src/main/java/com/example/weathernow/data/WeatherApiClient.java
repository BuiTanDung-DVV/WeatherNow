package com.example.weathernow.data;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherApiClient {

    private WeatherApiService apiService;
    private WeatherDao weatherDao;  // Dao để lưu vào cơ sở dữ liệu

    public WeatherApiClient(WeatherApiService apiService, WeatherDao weatherDao) {
        this.apiService = apiService;
        this.weatherDao = weatherDao;
    }

    public void fetchWeatherData(String city, String apiKey) {
        Call<WeatherEntity> call = apiService.getWeather(city, apiKey);

        call.enqueue(new Callback<WeatherEntity>() {
            @Override
            public void onResponse(Call<WeatherEntity> call, Response<WeatherEntity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherEntity weatherEntity = response.body();
                    weatherEntity.setTimestamp(System.currentTimeMillis());  // Set timestamp cho dữ liệu

                    // Lưu vào cơ sở dữ liệu
                    saveWeatherData(weatherEntity);
                }
            }

            @Override
            public void onFailure(Call<WeatherEntity> call, Throwable t) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void saveWeatherData(WeatherEntity weatherEntity) {
        // Sử dụng dao để lưu vào cơ sở dữ liệu
        weatherDao.insertWeather(weatherEntity);
    }
}
