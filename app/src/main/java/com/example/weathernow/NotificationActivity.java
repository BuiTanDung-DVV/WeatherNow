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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.weathernow.data.AppDatabase;
import com.example.weathernow.data.WeatherEntity;

public class NotificationActivity extends AppCompatActivity {

    private Switch switchNotification;
    private SharedPreferences sharedPreferences;
    private AppDatabase appDatabase; // Cơ sở dữ liệu Room
    private TextView weatherDetailsTextView; // TextView để hiển thị thông tin thời tiết
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1;

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
        fetchWeatherDataAndShowNotification();


        sharedPreferences = getSharedPreferences("WeatherNowSettings", MODE_PRIVATE);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Khởi tạo Switch và kiểm tra trạng thái
            switchNotification = findViewById(R.id.switchNotification);
            boolean isNotificationsEnabled = sharedPreferences.getBoolean("notifications", false);
            switchNotification.setChecked(isNotificationsEnabled);

            switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
                toggleNotification(isChecked);
            });
    }

    private void fetchWeatherDataAndShowNotification() {
        String selectedCity = "Hà Nội"; // Giả sử thành phố bạn muốn lấy dữ liệu từ Room

        // Lấy dữ liệu từ Room trong một thread riêng
        new Thread(() -> {
            WeatherEntity latestWeather = appDatabase.weatherDao().getLatestWeatherByCity(selectedCity);

            runOnUiThread(() -> {
                if (latestWeather != null) {
                    // Cập nhật UI với thông tin thời tiết từ cơ sở dữ liệu
                    String weatherDetails = "Nhiệt độ: " + latestWeather.getTemperature() + "°C\n" +
                            "Độ ẩm: " + latestWeather.getHumidity() + "%\n" +
                            "Gió: " + latestWeather.getWindSpeed() + " m/s";
                    weatherDetailsTextView.setText(weatherDetails);

                    // Tạo thông báo hệ thống
                    createWeatherNotification(latestWeather);
                } else {
                    // Nếu không có dữ liệu thời tiết, hiển thị thông báo lỗi
                    weatherDetailsTextView.setText("Không có dữ liệu thời tiết.");
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
                showSystemNotification("Thông báo hệ thống đã được bật.");
            } else {
                // Quyền bị từ chối, thông báo cho người dùng
                Toast.makeText(this, "Không có quyền gửi thông báo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showSystemNotification(String message) {
        // Kiểm tra quyền gửi thông báo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Nếu chưa cấp quyền, yêu cầu cấp quyền
                Toast.makeText(this, "Cần cấp quyền thông báo", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Quyền thông báo bị từ chối", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Xử lý các lỗi khác nếu cần
            e.printStackTrace();
        }
    }

    private void toggleNotification(boolean isChecked) {
        // Lưu trạng thái bật/tắt thông báo vào SharedPreferences
        sharedPreferences.edit().putBoolean("notifications", isChecked).apply();  // Lưu giá trị
        String message = isChecked ? "Thông báo đã bật" : "Thông báo đã tắt";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        if (isChecked) {
            createNotificationChannel();
            showSystemNotification(message);  // Hiển thị thông báo hệ thống
        } else {
            // Nếu thông báo bị tắt, có thể không cần tạo kênh hoặc gửi thông báo nữa.
        }
    }
    private void createWeatherNotification(WeatherEntity weather) {
        // Tạo PendingIntent cho notification (có thể mở activity khi người dùng nhấn vào thông báo)
        Intent intent = new Intent(this, NotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        // Tạo notification với thông tin thời tiết
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "weather_channel")
                .setSmallIcon(R.drawable.ic_weather) // Icon của notification
                .setContentTitle("Weather Notification: " + weather.getCity())
                .setContentText("Temperature: " + weather.getTemperature() + "°C, WindSpeed: " + weather.getWindSpeed() + " m/s")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Đóng thông báo khi nhấn vào

        // Hiển thị thông báo
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
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
