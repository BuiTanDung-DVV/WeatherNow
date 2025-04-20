package com.example.weathernow;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        TextView privacyTextView = findViewById(R.id.privacyTextView);
        privacyTextView.setText("Đây là nội dung Chính sách bảo mật của ứng dụng WeatherNow. Chúng tôi cam kết bảo vệ thông tin cá nhân của bạn.");
    }
}