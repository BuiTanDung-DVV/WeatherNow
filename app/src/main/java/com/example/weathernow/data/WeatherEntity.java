package com.example.weathernow.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weather")
public class WeatherEntity {

        @PrimaryKey(autoGenerate = true)
        public int id;

        @ColumnInfo(name = "city")
        public String city;

        @ColumnInfo(name = "temperature")
        public double temperature;

        @ColumnInfo(name = "latitude")
        public double latitude;  // Thêm trường latitude

        @ColumnInfo(name = "longitude")
        public double longitude; // Thêm trường longitude

        @ColumnInfo(name = "timestamp")
        public long timestamp;
}



