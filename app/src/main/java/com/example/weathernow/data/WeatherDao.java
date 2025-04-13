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


    @Query("SELECT * FROM weather WHERE city = :city LIMIT 1")
    LiveData<WeatherEntity> getWeatherByCity(String city);

    @Query("SELECT * FROM weather WHERE latitude = :latitude AND longitude = :longitude LIMIT 1")
    LiveData<WeatherEntity> getWeatherByCoordinates(double latitude, double longitude);

    @Query("SELECT * FROM weather")
    List<WeatherEntity> getAll();

    @Query("DELETE FROM weather")
    void deleteAll();
}
