package com.example.weathernow.model;

import com.example.weathernow.R;

public class HourlyForecast {
    private String time;
    private double temperature;
    private String iconCode;
    private String description;
    
    public HourlyForecast(String time, double temperature, String iconCode, String description) {
        this.time = time;
        this.temperature = temperature;
        this.iconCode = iconCode;
        this.description = description;
    }
    
    public String getTime() { return time; }
    public double getTemperature() { return temperature; }
    public String getIconCode() { return iconCode; }
    public String getDescription() { return description; }
    
    public String getIconUrl() {
        return "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
    }
    
    public int getIconResourceId() {
        // Chuyển đổi mã icon từ API sang resource ID
        switch (iconCode) {
            case "01d": return R.drawable.ic_clear; // Trời nắng ban ngày
            case "01n": return R.drawable.ic_fullmoon; // Trời quang ban đêm
            case "02d": return R.drawable.ic_sunny;// Mây thưa ban ngày
            case "02n": return R.drawable.ic_night; // Mây thưa ban đêm
            case "03d": return R.drawable.ic_cloudy;
            case "03n": return R.drawable.ic_cloudy; // Nhiều mây
            case "04d": return R.drawable.ic_cloudy;
            case "04n": return R.drawable.ic_night_cloudy; // Mây đen
            case "09d": return R.drawable.ic_rainy;
            case "09n": return R.drawable.ic_night_rain; // Mưa rào
            case "10d": return R.drawable.ic_cloudy_rainy; // Mưa
            case "10n": return R.drawable.ic_night_rain; // Mưa
            case "11d": return R.drawable.ic_thunder; // Giông bão
            case "11n": return R.drawable.ic_thunder; // Giông bão
            case "13d": return R.drawable.ic_cloudy;
            case "13n": return R.drawable.ic_cloudy; // Tuyết
            case "50d": return R.drawable.ic_cloudy;
            case "50n": return R.drawable.ic_cloudy; // Sương mù
            default: return R.drawable.ic_cloudy; // Mặc định
        }
    }
} 