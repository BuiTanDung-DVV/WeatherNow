package com.example.weathernow.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WeatherNotificationWorker extends Worker {

    public WeatherNotificationWorker(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        // Tạo thông báo
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Kiểm tra Android 8 trở lên để tạo channel thông báo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "weather_notification_channel";
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Weather Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo thông báo
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), "weather_notification_channel")
                .setContentTitle("Thông báo thời tiết")
                .setContentText("Đây là thông báo thời tiết mới nhất!")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        // Hiển thị thông báo
        notificationManager.notify(1, notification);

        // Trả về kết quả thành công
        return Result.success();
    }
}
