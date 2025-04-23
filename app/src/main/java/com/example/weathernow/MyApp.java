package com.example.weathernow;

import android.app.Application;
import android.content.Context;

import com.example.weathernow.helper.LocaleHelper;

public class MyApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        String lang = LocaleHelper.getStoredLanguage(base);
        super.attachBaseContext(LocaleHelper.setLocale(base, lang));
    }
}
