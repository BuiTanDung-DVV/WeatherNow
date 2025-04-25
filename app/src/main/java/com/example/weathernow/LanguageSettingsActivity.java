package com.example.weathernow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weathernow.helper.LocaleHelper;

public class LanguageSettingsActivity extends AppCompatActivity {

    private RadioGroup languageRadioGroup;
    private RadioButton radioVietnamese, radioEnglish, radioFrench;
    private SharedPreferences sharedPreferences;


    @Override
    protected void attachBaseContext(Context newBase) {
        // Lấy ngôn ngữ đã lưu từ SharedPreferences và thiết lập lại ngữ cảnh (context) với ngôn ngữ đó
        String language = LocaleHelper.getStoredLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);

        sharedPreferences = getSharedPreferences("language_prefs", MODE_PRIVATE);

        languageRadioGroup = findViewById(R.id.languageRadioGroup);
        radioVietnamese = findViewById(R.id.radioVietnamese);
        radioEnglish = findViewById(R.id.radioEnglish);
        radioFrench = findViewById(R.id.radioFrench);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

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

        // Gán sự kiện khi người dùng thay đổi lựa chọn ngôn ngữ
        languageRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String languageCode;
            if (checkedId == R.id.radioVietnamese) {
                languageCode = "vi";
            } else if (checkedId == R.id.radioEnglish) {
                languageCode = "en";
            } else {
                languageCode = "fr";
            }
            // Gọi hàm thay đổi ngôn ngữ
            changeLanguage(languageCode);
        });
    }

    private void changeLanguage(String language) {
        // Lưu lại ngôn ngữ mới được chọn vào SharedPreferences
        getSharedPreferences("language_prefs", MODE_PRIVATE)
                .edit()
                .putString("language", language)
                .apply();

        // Khởi động lại MainActivity, đồng thời xóa toàn bộ stack của Activity trước đó để đảm bảo ngôn ngữ được áp dụng từ đầu
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Đóng LanguageSettingsActivity
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Khi activity quay lại trạng thái foreground, cập nhật lại ngôn ngữ nếu có thay đổi
        String currentLang = LocaleHelper.getStoredLanguage(this);
        LocaleHelper.updateLocale(this, currentLang);
        // Nếu muốn có thể thêm xử lý reload Activity nếu cần thiết
    }
}
