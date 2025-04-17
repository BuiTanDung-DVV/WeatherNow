package com.example.weathernow;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
            saveLanguage(languageCode);
        });
    }

    private void saveLanguage(String languageCode) {
        // Lưu ngôn ngữ vào SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", languageCode);
        editor.apply();

        Toast.makeText(this, "Ngôn ngữ đã được thay đổi", Toast.LENGTH_SHORT).show();
        recreate(); // BaseActivity tự cập nhật ngôn ngữ
    }

//    private void setLocale(String languageCode) {
//        Locale locale = new Locale(languageCode);
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
//
//        // Thông báo cho người dùng
//        String message = "Ngôn ngữ đã được thay đổi";
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//
//        // Khởi động lại Activity để áp dụng ngôn ngữ mới
//        recreate();
//    }
}