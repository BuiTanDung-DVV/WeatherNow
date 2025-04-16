package com.example.weathernow.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WeatherEntity> weathers);

    @Insert
    void insertWeather(WeatherEntity weatherEntity);
    @Query("SELECT * FROM weather WHERE city = :city ORDER BY timestamp DESC LIMIT 1")
    WeatherEntity getLatestWeatherByCity(String city);

    @Query("SELECT * FROM weather")
    List<WeatherEntity> getAll();

    @Query("DELETE FROM weather")
    void deleteAll();
}
