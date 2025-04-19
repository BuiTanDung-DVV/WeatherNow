package com.example.weathernow.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {

    @GET("weather")
    Call<JsonObject> getWeatherByCity(
            @Query("q") String cityName,
            @Query("units") String units,
            @Query("lang") String language
    );

    @GET("forecast")
    Call<JsonObject> getForecastByCity(
            @Query("q") String city,
            @Query("units") String units,
            @Query("lang") String lang
    );

}