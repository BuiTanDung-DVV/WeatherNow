package com.example.weathernow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout notificationSetting, termsSetting, privacySetting, languageSetting;
    private SharedPreferences sharedPreferences;
    private ImageButton btnBack;

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

        // Thực hiện các thao tác chuyển màn hình
        notificationSetting.setOnClickListener(view -> openNotificationSettings());
        languageSetting.setOnClickListener(v -> openLanguageSettings());
        termsSetting.setOnClickListener(v -> openTermsAndConditions());
        privacySetting.setOnClickListener(v -> openPrivacyPolicy());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void openNotificationSettings() {
        // Chuyển sang màn hình cài đặt thông báo
        Intent intent = new Intent(this, NotificationActivity.class);
        startActivity(intent);
    }

    private void openLanguageSettings() {
        // Chuyển sang màn hình cài đặt ngôn ngữ
        Intent intent = new Intent(this, LanguageSettingsActivity.class);
        startActivity(intent);
    }

    private void openTermsAndConditions() {
        // Chuyển sang màn hình điều khoản sử dụng
        Intent intent = new Intent(this, TermsActivity.class);
        startActivity(intent);
    }

    private void openPrivacyPolicy() {
        // Chuyển sang màn hình chính sách bảo mật
        Intent intent = new Intent(this, PrivacyActivity.class);
        startActivity(intent);
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
