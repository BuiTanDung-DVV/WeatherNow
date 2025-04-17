package com.example.weathernow.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;

public class ApiKeyProvider {

    private static final String TAG = "ApiKeyProvider";

    public static String getApiKey(Context context) {
        String apiKey = "";

        try {
            InputStream inputStream = context.getAssets().open("weather-api-key.json");
            JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
            apiKey = json.get("API_Key").getAsString();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đọc API key: " + e.getMessage(), e);
        }

        return apiKey;
    }
}