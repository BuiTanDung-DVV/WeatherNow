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
        Intent intent = new Intent(this, LanguageSettingsActivity.class);
        startActivity(intent);
    }

    private void openTermsAndConditions() {
        Intent intent = new Intent(this, TermsActivity.class);
        startActivity(intent);
    }

    private void openPrivacyPolicy() {
        Intent intent = new Intent(this, PrivacyActivity.class);
        startActivity(intent);
    }
    private void openNotificationActivity(){
        Intent intent = new Intent(this, NotificationActivity.class);
        String selectedCity = getIntent().getStringExtra("selectedCity");

        intent.putExtra("selectedCity", selectedCity);

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentLang = LocaleHelper.getStoredLanguage(this);
        LocaleHelper.updateLocale(this, currentLang);
    }


}