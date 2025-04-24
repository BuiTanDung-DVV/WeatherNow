package com.example.weathernow;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weathernow.helper.LocaleHelper;

public class PrivacyActivity extends AppCompatActivity {

    protected void attachBaseContext(Context newBase) {
        String language = LocaleHelper.getStoredLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView privacyTextView = findViewById(R.id.privacyTextView);
        privacyTextView.setText(R.string.privacy_title);
    }
    @Override
    protected void onResume() {
        super.onResume();
        String currentLang = LocaleHelper.getStoredLanguage(this);
        LocaleHelper.updateLocale(this, currentLang);
    }
}