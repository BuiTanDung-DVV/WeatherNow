package com.example.weathernow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.os.Build;


import androidx.appcompat.app.AppCompatActivity;

import com.example.weathernow.helper.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout notificationLayout, termsSetting, privacySetting, languageSetting;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Lấy ngôn ngữ đã lưu và áp dụng
        String language = LocaleHelper.getStoredLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationLayout = findViewById(R.id.notificationLayout);
        termsSetting = findViewById(R.id.termsLayout);
        privacySetting = findViewById(R.id.privacyLayout);
        languageSetting = findViewById(R.id.languageLayout);

        ImageButton btnBack = findViewById(R.id.btnBack);

        notificationLayout.setOnClickListener(v -> openNotificationActivity());
        languageSetting.setOnClickListener(v -> openLanguageSettings());
        termsSetting.setOnClickListener(v -> openTermsAndConditions());
        privacySetting.setOnClickListener(v -> openPrivacyPolicy());
        btnBack.setOnClickListener(v -> finish());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

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
    private void openNotificationActivity(){
        Intent intent = new Intent(this, NotificationActivity.class);
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
    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra và cập nhật ngôn ngữ
        String currentLang = LocaleHelper.getStoredLanguage(this);
        LocaleHelper.updateLocale(this, currentLang);
         // Tải lại Activity nếu ngôn ngữ thay đổi (tùy chọn)
    }


}