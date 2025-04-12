package com.example.weathernow.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    public static void setLocale(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        // Dùng createConfigurationContext thay cho updateConfiguration để đảm bảo tương thích với Android Pie trở lên
        Context contextUpdated = context.createConfigurationContext(config);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        // Lưu lại ngôn ngữ người dùng chọn
        SharedPreferences prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        prefs.edit().putString("App_Lang", langCode).apply();
    }

    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return prefs.getString("App_Lang", "en"); // mặc định là tiếng Anh
    }

    public static void applySavedLocale(Context context) {
        setLocale(context, getSavedLanguage(context));
    }
}
