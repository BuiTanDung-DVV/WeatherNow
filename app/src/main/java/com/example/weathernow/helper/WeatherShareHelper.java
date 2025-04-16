package com.example.weathernow.helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.example.weathernow.R;
import com.example.weathernow.data.WeatherEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class WeatherShareHelper {

    private static final String TAG = "WeatherShareHelper";

    // Hàm chính: Tạo ảnh từ dữ liệu thời tiết và chia sẻ
    public static void shareWeatherCard(Context context, WeatherEntity weather) {
        if (weather == null) {
            Log.e(TAG, "Không có dữ liệu thời tiết để chia sẻ.");
            return;
        }

        // Inflate layout share_card
        LayoutInflater inflater = LayoutInflater.from(context);
        View shareView = inflater.inflate(R.layout.weather_share_card, null);

        // Gán dữ liệu vào layout
        TextView cityText = shareView.findViewById(R.id.shareCity);
        TextView tempText = shareView.findViewById(R.id.shareTemp);
        TextView descText = shareView.findViewById(R.id.shareDesc);
        TextView humidityText = shareView.findViewById(R.id.shareHumidity);
        TextView windText = shareView.findViewById(R.id.shareWind);

        cityText.setText(weather.getCity());
        tempText.setText(String.format(Locale.getDefault(), "%.1f°C", weather.getTemperature()));
        descText.setText(weather.getDescription());
        humidityText.setText("Độ ẩm: " + weather.getHumidity() + "%");
        windText.setText("Gió: " + weather.getWindSpeed() + " m/s");

        // Chuẩn bị view để chụp
        int specWidth = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY);
        int specHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        shareView.measure(specWidth, specHeight);
        shareView.layout(0, 0, shareView.getMeasuredWidth(), shareView.getMeasuredHeight());

        Bitmap bitmap = captureView(shareView);

        if (bitmap != null) {
            Uri imageUri = saveBitmapToCache(context, bitmap);
            if (imageUri != null) {
                shareImage(context, imageUri);
            }
        }
    }

    // Chụp ảnh từ view
    private static Bitmap captureView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // Lưu ảnh vào cache để chia sẻ
    private static Uri saveBitmapToCache(Context context, Bitmap bitmap) {
        try {
            File cachePath = new File(context.getCacheDir(), "images");
            if (!cachePath.exists()) cachePath.mkdirs();
            File file = new File(cachePath, "weather_share.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi lưu ảnh: ", e);
            return null;
        }
    }

    // Gửi ảnh qua Intent
    private static void shareImage(Context context, Uri uri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ thời tiết qua..."));
    }
}
