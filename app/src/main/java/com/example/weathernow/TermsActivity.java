package com.example.weathernow;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        TextView termsTextView = findViewById(R.id.termsTextView);
        termsTextView.setText("Đây là nội dung Điều khoản sử dụng của ứng dụng WeatherNow. Vui lòng đọc kỹ trước khi sử dụng.");
    }
}