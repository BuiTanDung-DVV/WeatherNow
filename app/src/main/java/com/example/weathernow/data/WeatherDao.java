package com.example.weathernow.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface WeatherDao {

    @Insert
    void insert(WeatherEntity weather);

    @Query("SELECT * FROM weather ORDER BY timestamp DESC")
    List<WeatherEntity> getAllWeather();

    @Query("SELECT * FROM weather WHERE city = :city LIMIT 1")
    LiveData<WeatherEntity> getWeatherByCity(String city); // LiveData to observe data changes
}

