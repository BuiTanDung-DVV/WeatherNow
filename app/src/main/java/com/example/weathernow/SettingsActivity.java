package com.example.weathernow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SettingsActivity extends BaseActivity {

    private LinearLayout notificationSetting, termsSetting, privacySetting, languageSetting;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

/*        notificationSetting = findViewById(R.id.notificationSetting);
        termsSetting = findViewById(R.id.termsSetting);
        privacySetting = findViewById(R.id.privacySetting);
        languageSetting = findViewById(R.id.languageSetting);*/

        sharedPreferences = getSharedPreferences("WeatherNowSettings", MODE_PRIVATE);

        ImageButton btnBack = findViewById(R.id.btnBack);

        notificationSetting.setOnClickListener(v -> toggleNotification());
        languageSetting.setOnClickListener(v -> openLanguageSettings());
        termsSetting.setOnClickListener(v -> openTermsAndConditions());
        privacySetting.setOnClickListener(v -> openPrivacyPolicy());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

    }
    private void toggleNotification() {
        // Đọc trạng thái thông báo hiện tại
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications", true);

        // Lưu trạng thái mới (đảo ngược)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notifications", !notificationsEnabled);
        editor.apply();

        // Thông báo cho người dùng
        String message = notificationsEnabled ? "Thông báo đã tắt" : "Thông báo đã bật";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        createNotificationChannel(); // chỉ cần gọi 1 lần
        showSystemNotification(message); // hiện trên thanh thông báo
    }

    private void openLanguageSettings() {
        // Mở màn hình cài đặt ngôn ngữ
        Intent intent = new Intent(this, LanguageSettingsActivity.class);
        startActivity(intent);
    }

    private void openTermsAndConditions() {
        // Mở màn hình điều khoản sử dụng
        Intent intent = new Intent(this, TermsActivity.class);
        startActivity(intent);
    }

    private void openPrivacyPolicy() {
        // Mở màn hình chính sách bảo mật
        Intent intent = new Intent(this, PrivacyActivity.class);
        startActivity(intent);
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Kênh thông báo chính";
            String description = "Thông báo từ ứng dụng WeatherNow";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("weather_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showSystemNotification(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Chưa được cấp quyền, không hiển thị thông báo
                Toast.makeText(this, "Cần cấp quyền thông báo", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "weather_channel")
                .setSmallIcon(R.drawable.ic_notification) // đảm bảo bạn có icon này trong drawable
                .setContentTitle("WeatherNow")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1001, builder.build());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền thông báo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bạn đã từ chối quyền thông báo", Toast.LENGTH_SHORT).show();
            }
        }
    }


}