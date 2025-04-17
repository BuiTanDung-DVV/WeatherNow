package com.example.weathernow.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {WeatherEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WeatherDao weatherDao();
}
