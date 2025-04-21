package com.example.weathernow.helper;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.weathernow.data.AppDatabase;
import com.example.weathernow.data.NotificationEntity;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String content = "A sunny day in your location, consider wearing your UV protection";
        long timestamp = System.currentTimeMillis();

        // Tạo notification với constructor thay vì set trực tiếp các thuộc tính
        NotificationEntity notification = new NotificationEntity(content, timestamp);

        // Sử dụng getInstance thay vì tạo database instance mới
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        new Thread(() -> db.notificationDao().insert(notification)).start();

        return Result.success();
    }
}
