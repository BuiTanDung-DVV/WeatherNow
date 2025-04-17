package com.example.weathernow.firebase;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import com.example.weathernow.data.WeatherEntity;

public class FirestoreManager {
    private final FirebaseFirestore db;

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    public void saveWeatherData(String uid, WeatherEntity weather) {
        Map<String, Object> data = new HashMap<>();
        data.put("city", weather.city);
        data.put("temperature", weather.temperature);
        data.put("timestamp", weather.timestamp);

        db.collection("weatherLogs")
                .document(uid)
                .collection("entries")
                .add(data);
    }
}

