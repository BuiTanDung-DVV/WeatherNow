package com.example.weathernow.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weather")
public class WeatherEntity {

        @PrimaryKey(autoGenerate = true)
        private int id;

        @ColumnInfo(name = "city")
        private String city;

        @ColumnInfo(name = "temperature")
        private double temperature;

        @ColumnInfo(name = "description")
        private String description;

        @ColumnInfo(name = "humidity")
        private int humidity;

        @ColumnInfo(name = "wind_speed")
        private double windSpeed;

        @ColumnInfo(name = "latitude")
        private double latitude;

        @ColumnInfo(name = "longitude")
        private double longitude;

        @ColumnInfo(name = "timestamp")
        private long timestamp;

        // Getters và Setters

        public int getId() {
                return id;
        }
        public void setId(int id) {
                this.id = id;
        }

        public String getCity() {
                return city;
        }
        public void setCity(String city) {
                this.city = city;
        }

        public double getTemperature() {
                return temperature;
        }
        public void setTemperature(double temperature) {
                this.temperature = temperature;
        }

        public String getDescription() {
                return description;
        }
        public void setDescription(String description) {
                this.description = description;
        }

        public int getHumidity() {
                return humidity;
        }
        public void setHumidity(int humidity) {
                this.humidity = humidity;
        }

        public double getWindSpeed() {
                return windSpeed;
        }
        public void setWindSpeed(double windSpeed) {
                this.windSpeed = windSpeed;
        }

        public double getLatitude() {
                return latitude;
        }
        public void setLatitude(double latitude) {
                this.latitude = latitude;
        }

        public double getLongitude() {
                return longitude;
        }
        public void setLongitude(double longitude) {
                this.longitude = longitude;
        }

        public long getTimestamp() {
                return timestamp;
        }
        public void setTimestamp(long timestamp) {
                this.timestamp = timestamp;
        }
}