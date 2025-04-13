package com.example.weathernow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout notificationSetting, termsSetting, privacySetting;
    private Spinner languageSpinner;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationSetting = findViewById(R.id.notificationSetting);
        termsSetting = findViewById(R.id.termsSetting);
        privacySetting = findViewById(R.id.privacySetting);
        languageSpinner = findViewById(R.id.languageSpinner);

        sharedPreferences = getSharedPreferences("WeatherNowSettings", MODE_PRIVATE);

        // Lưu ngôn ngữ
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", "en"); // Lưu ngôn ngữ là English
        editor.apply();

        // Lấy ngôn ngữ
        String language = sharedPreferences.getString("language", "vi"); // Mặc định là Tiếng Việt

        // Lưu trạng thái thông báo
        editor.putBoolean("notifications", true); // Bật thông báo
        editor.apply();

        // Lấy trạng thái thông báo
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications", true); // Mặc định là bật

        // Cấu hình Spinner ngôn ngữ
        setupLanguageSpinner();

        // Xử lý sự kiện cho các mục cài đặt
        notificationSetting.setOnClickListener(v -> toggleNotification());
        termsSetting.setOnClickListener(v -> openTermsAndConditions());
        privacySetting.setOnClickListener(v -> openPrivacyPolicy());

        // Xử lý sự kiện cho nút quay lại MainActivity
        Button btnBackToMain = findViewById(R.id.btnBackToMain);
        btnBackToMain.setOnClickListener(v -> {
            finish(); // Đóng SettingsActivity và quay lại MainActivity
        });
    }

    private void setupLanguageSpinner() {
        // Danh sách ngôn ngữ
        String[] languages = {"Tiếng Việt", "English", "Français"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // Load ngôn ngữ hiện tại từ SharedPreferences
        String currentLanguage = sharedPreferences.getString("language", "vi");
        switch (currentLanguage) {
            case "vi":
                languageSpinner.setSelection(0);
                break;
            case "en":
                languageSpinner.setSelection(1);
                break;
            case "fr":
                languageSpinner.setSelection(2);
                break;
        }

        // Lưu ngôn ngữ khi người dùng thay đổi
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedLanguage = "vi"; // Mặc định là Tiếng Việt
                switch (position) {
                    case 0:
                        selectedLanguage = "vi";
                        break;
                    case 1:
                        selectedLanguage = "en";
                        break;
                    case 2:
                        selectedLanguage = "fr";
                        break;
                }
                saveLanguage(selectedLanguage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì nếu không có ngôn ngữ nào được chọn
            }
        });
    }

    private void saveLanguage(String languageCode) {
        // Lưu ngôn ngữ vào SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", languageCode);
        editor.apply();

        // Cập nhật ngôn ngữ ứng dụng
        setLocale(languageCode);
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Thông báo cho người dùng
        Toast.makeText(this, "Ngôn ngữ đã được thay đổi", Toast.LENGTH_SHORT).show();

        // Khởi động lại Activity để áp dụng ngôn ngữ mới
        recreate();
    }

    private void toggleNotification() {
        // Xử lý bật/tắt thông báo (giả sử bạn lưu trạng thái thông báo trong SharedPreferences)
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notifications", !notificationsEnabled);
        editor.apply();

        // Thông báo cho người dùng
        String message = notificationsEnabled ? "Thông báo đã tắt" : "Thông báo đã bật";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void openTermsAndConditions() {
        // Mở màn hình điều khoản sử dụng
        Intent intent = new Intent(this, TermsActivity.class);
        startActivity(intent);
    }

    private void openPrivacyPolicy() {
        // Mở màn hình chính sách bảo mật
        Intent intent = new Intent(this, PrivacyPolicyActivity.class);
        startActivity(intent);
    }
}