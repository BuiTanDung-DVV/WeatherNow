package com.example.weathernow.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("data/2.5/weather")  // Đây là endpoint API của bạn (thay bằng endpoint thật)
    Call<WeatherEntity> getWeather(
            @Query("q") String city,
            @Query("appid") String apiKey);
}
