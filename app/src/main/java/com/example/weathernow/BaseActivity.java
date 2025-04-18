package com.example.weathernow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.res.Configuration;
import androidx.annotation.NonNull;
import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(@NonNull Context newBase) {
        // Lấy ngôn ngữ đã lưu
        Locale locale = new Locale(getStoredLanguage(newBase));
        Locale.setDefault(locale);

        // Tạo cấu hình mới với locale đã chọn
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);

        // Áp dụng cấu hình mới
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

    private String getStoredLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE);
        return prefs.getString("language_key", "vi");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra và cập nhật ngôn ngữ mỗi khi activity được hiển thị
        String currentLang = getStoredLanguage(this);
        updateLocaleIfNeeded(currentLang);
    }

    private void updateLocaleIfNeeded(String currentLang) {
        Locale currentLocale = Locale.getDefault();
        if (!currentLocale.getLanguage().equals(currentLang)) {
            Locale newLocale = new Locale(currentLang);
            Locale.setDefault(newLocale);
            Configuration config = new Configuration(getResources().getConfiguration());
            config.setLocale(newLocale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
            recreate();
        }
    }
}