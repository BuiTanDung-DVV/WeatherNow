// File: LocaleHelper.java
package com.example.weathernow;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import java.util.Locale;

public class LocaleHelper {

    // Lấy ngôn ngữ đã lưu từ SharedPreferences (nếu chưa có thì trả về "vi" là tiếng Việt)
    public static String getStoredLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE);
        return prefs.getString("language", "vi"); // Mặc định là tiếng Việt
    }

    // Thiết lập ngôn ngữ cho Context khi khởi tạo hoặc chuyển đổi
    public static Context setLocale(Context context, String language) {
        // Tạo đối tượng Locale mới với mã ngôn ngữ được chọn (vi, en, fr, ...)
        Locale locale = new Locale(language);
        Locale.setDefault(locale); // Đặt làm locale mặc định cho hệ thống

        // Lấy cấu hình hiện tại và áp dụng locale mới
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        // Trả về một Context mới với cấu hình locale mới
        return context.createConfigurationContext(config);
    }

    // Cập nhật locale cho ứng dụng hiện tại mà không tạo Context mới (thường dùng trong onResume)
    public static void updateLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        // Cập nhật lại cấu hình của ứng dụng
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        // Áp dụng thay đổi cấu hình cho Resource hiện tại
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

}
