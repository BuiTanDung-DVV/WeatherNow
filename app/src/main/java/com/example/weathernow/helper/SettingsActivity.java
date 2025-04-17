package com.example.weathernow.helper;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weathernow.R;

public class SettingsActivity extends AppCompatActivity {
    private String[] languages = {"English", "Tiếng Việt", "Français"};
    private String[] codes = {"en", "vi", "fr"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Áp dụng ngôn ngữ đã lưu khi mở Activity
        LocaleHelper.applySavedLocale(this);

        // Hiển thị hộp thoại để thay đổi ngôn ngữ
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.change_language))
                .setItems(languages, (dialog, which) -> {
                    LocaleHelper.setLocale(this, codes[which]); // Lưu ngôn ngữ đã chọn
                    recreate(); // Refresh lại Activity để áp dụng ngôn ngữ mới
                })
                .show();
    }
}
