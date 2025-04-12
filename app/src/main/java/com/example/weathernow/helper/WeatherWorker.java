package com.example.weathernow.helper;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.core.app.NotificationManagerCompat;

import com.example.weathernow.MainActivity;
import com.example.weathernow.R;

public class WeatherWorker extends Worker {

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Kiểm tra quyền gửi thông báo (Android 13 trở lên)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Nếu không có quyền, yêu cầu quyền trong Activity (bạn sẽ cần một phương thức trong Activity để xử lý việc yêu cầu quyền)
                return Result.failure(); // Trả về thất bại nếu quyền chưa được cấp
            }
        }

        // Tiến hành gửi thông báo
        sendNotification("Cập nhật thời tiết", "Đừng quên kiểm tra thời tiết hôm nay!");
        return Result.success();
    }

    private void sendNotification(String title, String message) {
        // Tạo notification channel nếu chưa tồn tại (chỉ cần cho Android 8.0 trở lên)
        createNotificationChannel();

        // Tạo notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "weather_channel")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_weather)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());

        // Kiểm tra và yêu cầu quyền nếu chưa được cấp
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Nếu quyền chưa được cấp, yêu cầu quyền trong Activity
            if (getApplicationContext() instanceof MainActivity) {
                // Gọi từ MainActivity để yêu cầu quyền
                ((MainActivity) getApplicationContext()).requestNotificationPermission();
            }
            return; // Dừng gửi thông báo cho đến khi quyền được cấp
        }

        // Nếu quyền đã được cấp, gửi thông báo
        manager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        // Tạo notification channel cho Android 8.0 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Weather Channel";
            String description = "Channel for weather notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("weather_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
