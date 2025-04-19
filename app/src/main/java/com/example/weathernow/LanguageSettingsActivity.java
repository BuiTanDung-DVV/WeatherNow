package com.example.weathernow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class LanguageSettingsActivity extends BaseActivity {

    private RadioGroup languageRadioGroup;
    private RadioButton radioVietnamese, radioEnglish, radioFrench;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("WeatherNowSettings", MODE_PRIVATE);

        // Tìm các view trong layout
        languageRadioGroup = findViewById(R.id.languageRadioGroup);
        radioVietnamese = findViewById(R.id.radioVietnamese);
        radioEnglish = findViewById(R.id.radioEnglish);
        radioFrench = findViewById(R.id.radioFrench);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Xử lý sự kiện cho nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Đặt ngôn ngữ đã chọn
        String currentLanguage = sharedPreferences.getString("language", "vi");
        switch (currentLanguage) {
            case "vi":
                radioVietnamese.setChecked(true);
                break;
            case "en":
                radioEnglish.setChecked(true);
                break;
            case "fr":
                radioFrench.setChecked(true);
                break;
        }

        // Lắng nghe sự kiện khi chọn ngôn ngữ
        languageRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String languageCode;
            if (checkedId == R.id.radioVietnamese) {
                languageCode = "vi";
            } else if (checkedId == R.id.radioEnglish) {
                languageCode = "en";
            } else {
                languageCode = "fr";
            }
            changeLanguage(languageCode);
        });
    }

    private void changeLanguage(String language) {
        // Lưu ngôn ngữ mới vào SharedPreferences
        sharedPreferences.edit()
            .putString("language", language)
            .apply();

        // Cập nhật cấu hình ngôn ngữ
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        // Khởi động lại MainActivity và xóa activity stack
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}