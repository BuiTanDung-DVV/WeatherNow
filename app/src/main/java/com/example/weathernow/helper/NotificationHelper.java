package com.example.weathernow.helper;

import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.weathernow.R;

public class NotificationHelper {
    public static void sendWeatherNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "weather_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Thông báo thời tiết")
                .setContentText("Đây là bản cập nhật thời tiết định kỳ")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1001, builder.build());
    }
}
