package com.example.weathernow;

import android.app.Application;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.weathernow.WeatherNotificationWorker;

import java.util.concurrent.TimeUnit;

public class WeatherNowApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Lên lịch thông báo mỗi 6 giờ
        scheduleWeatherNotification();
    }

    // Đặt phương thức này vào trong lớp Application
    public void scheduleWeatherNotification() {
        PeriodicWorkRequest weatherWork =
                new PeriodicWorkRequest.Builder(WeatherNotificationWorker.class, 6, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "WeatherNotification",
                ExistingPeriodicWorkPolicy.KEEP,
                weatherWork
        );
    }
}
