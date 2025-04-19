package com.example.weathernow.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {WeatherEntity.class, NotificationEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WeatherDao weatherDao();
    public abstract NotificationDao notificationDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "weather_database"
                            )
                            .fallbackToDestructiveMigration() // Tự động xoá DB nếu schema thay đổi
                            .build();

                }
            }
        }
        return INSTANCE;
    }
}