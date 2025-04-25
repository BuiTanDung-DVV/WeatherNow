package com.example.weathernow;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.weathernow.data.AppDatabase;
import com.example.weathernow.data.WeatherEntity;
import com.example.weathernow.helper.LocaleHelper;
import com.example.weathernow.helper.WeatherNotificationWorker;

import java.util.concurrent.TimeUnit;

public class NotificationActivity extends AppCompatActivity {

    private Switch switchNotification;
    private SharedPreferences sharedPreferences;
    private AppDatabase appDatabase; // Cơ sở dữ liệu Room
    private TextView weatherDetailsTextView; // TextView để hiển thị thông tin thời tiết
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1;
    private ImageButton btnBack;
    @Override
    protected void attachBaseContext(Context newBase) {
        // Lấy ngôn ngữ đã lưu và áp dụng
        String language = LocaleHelper.getStoredLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        weatherDetailsTextView = findViewById(R.id.textNotifications);
        appDatabase = AppDatabase.getInstance(this); // Khởi tạo AppDatabase
        btnBack = findViewById(R.id.btnBack);
        fetchWeatherDataAndShowNotification();


        sharedPreferences = getSharedPreferences("WeatherNowSettings", MODE_PRIVATE);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Khởi tạo Switch và kiểm tra trạng thái
        switchNotification = findViewById(R.id.switchNotification);
        boolean isNotificationsEnabled = sharedPreferences.getBoolean("notifications", false);
        switchNotification.setChecked(isNotificationsEnabled);
        btnBack.setOnClickListener( v -> finish());
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleNotification(isChecked);
        });
    }

    private void fetchWeatherDataAndShowNotification() {
        String selectedCity = "Hà Nội"; // thành phố muốn lấy dữ liệu từ Room

        // Lấy dữ liệu từ Room trong một thread riêng
        new Thread(() -> {
            WeatherEntity latestWeather = appDatabase.weatherDao().getLatestWeatherByCity(selectedCity);

            runOnUiThread(() -> {
                if (latestWeather != null) {
                    // Cập nhật UI với thông tin thời tiết từ cơ sở dữ liệu
                    String weatherDetails = getString(R.string.notification_temp) + latestWeather.getTemperature() + "°C\n" +
                            getString(R.string.notification_humidity) + latestWeather.getHumidity() + "%\n" +
                            getString(R.string.notification_wind) + latestWeather.getWindSpeed() + " m/s";
                    weatherDetailsTextView.setText(weatherDetails);

                    // Tạo thông báo hệ thống
                    createWeatherNotification(this, latestWeather);
                } else {
                    // Nếu không có dữ liệu thời tiết, hiển thị thông báo lỗi
                    weatherDetailsTextView.setText(getString(R.string.no_weather_data));
                }
            });
        }).start();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, tiếp tục thực hiện thông báo
                showSystemNotification(getString(R.string.notification_enabled));
            } else {
                // Quyền bị từ chối, thông báo cho người dùng
                Toast.makeText(this, getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showSystemNotification(String message) {
        // Kiểm tra quyền gửi thông báo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Nếu chưa cấp quyền, yêu cầu cấp quyền
                Toast.makeText(this, getString(R.string.need_enable_notification), Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION_PERMISSION);
                return;
            }
        }

        // Tiến hành gửi thông báo nếu đã có quyền
        try {
            // Tiến hành gửi thông báo nếu có quyền
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "weather_channel")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("WeatherNow")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1001, builder.build());
        } catch (SecurityException e) {
            // Xử lý khi quyền bị từ chối
            Toast.makeText(this, getString(R.string.notification_permission_denied1), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Xử lý các lỗi khác nếu cần
            e.printStackTrace();
        }
    }

    private void toggleNotification(boolean isChecked) {
        // Lưu trạng thái bật/tắt thông báo
        sharedPreferences.edit().putBoolean("notifications", isChecked).apply();  // Lưu giá trị
        String message = isChecked ? getString(R.string.notification_enabled1) : getString(R.string.notification_disabled);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        if (isChecked) {
            createNotificationChannel();
            showSystemNotification(message);  // Hiển thị thông báo hệ thống

            // Lên lịch WeatherNotificationWorker chạy định kỳ
            PeriodicWorkRequest periodicWorkRequest =
                    new PeriodicWorkRequest.Builder(WeatherNotificationWorker.class, 6, TimeUnit.HOURS)
                            .build();
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "weather_notification_work",
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWorkRequest);
        } else {
            WorkManager.getInstance(this).cancelUniqueWork("weather_notification_work");
        }
    }
    public static void createWeatherNotification(Context context, WeatherEntity weather) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(context, NotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "weather_channel")
                .setSmallIcon(R.drawable.ic_weather)
                .setContentTitle("Weather Notification: " + weather.getCity())
                .setContentText("Temperature: " + weather.getTemperature() + "°C, WindSpeed: " + weather.getWindSpeed() + " m/s")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, notificationBuilder.build());
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "weather_channel";
            CharSequence name = "Main Notification Channel";
            String description = "Notification channal from WeatherNow";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra và cập nhật ngôn ngữ
        String currentLang = LocaleHelper.getStoredLanguage(this);
        LocaleHelper.updateLocale(this, currentLang);

    }

}