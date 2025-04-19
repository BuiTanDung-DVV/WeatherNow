package com.example.weathernow.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert
    void insert(NotificationEntity notification);

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    List<NotificationEntity> getAll();
}
