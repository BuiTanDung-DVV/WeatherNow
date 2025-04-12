package com.example.weathernow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weather")
public class WeatherEntity {
        @PrimaryKey(autoGenerate = true)
        public int id;
        public String city;
        public double temperature;
        public long timestamp;
    }


