package com.example.weathernow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SettingsActivity extends BaseActivity {

    private LinearLayout notificationLayout, termsLayout, privacyLayout, languageLayout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationLayout = findViewById(R.id.notificationLayout);
        termsLayout = findViewById(R.id.termsLayout);
        privacyLayout = findViewById(R.id.privacyLayout);
        languageLayout = findViewById(R.id.languageLayout);

        sharedPreferences = getSharedPreferences("WeatherNowSettings", MODE_PRIVATE);

        ImageButton btnBack = findViewById(R.id.btnBack);

        notificationLayout.setOnClickListener(v -> toggleNotification());
        languageLayout.setOnClickListener(v -> openLanguageSettings());
        termsLayout.setOnClickListener(v -> openTermsAndConditions());
        privacyLayout.setOnClickListener(v -> openPrivacyPolicy());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

    }
} 