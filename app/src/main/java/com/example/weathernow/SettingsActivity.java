package com.example.weathernow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout notificationSetting, termsSetting, privacySetting, languageSetting;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationSetting = findViewById(R.id.notificationSetting);
        termsSetting = findViewById(R.id.termsSetting);
        privacySetting = findViewById(R.id.privacySetting);
        languageSetting = findViewById(R.id.languageSetting);

        sharedPreferences = getSharedPreferences("WeatherNowSettings", MODE_PRIVATE);

        ImageButton btnBack = findViewById(R.id.btnBack);

        notificationSetting.setOnClickListener(v -> toggleNotification());
        languageSetting.setOnClickListener(v -> openLanguageSettings());
        termsSetting.setOnClickListener(v -> openTermsAndConditions());
        privacySetting.setOnClickListener(v -> openPrivacyPolicy());
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
}