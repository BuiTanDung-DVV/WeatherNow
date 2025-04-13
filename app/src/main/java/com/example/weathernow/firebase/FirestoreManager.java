package com.example.weathernow.firebase;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.weathernow.data.WeatherDao;
import com.example.weathernow.data.WeatherEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Quản lý đồng bộ dữ liệu với Firestore
public class FirestoreManager {

    private static final String TAG = "FirestoreManager";
    private static final String DEFAULT_UID = "local_user"; // Dùng khi không login
    private final FirebaseFirestore db;

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    // Lấy UID hiện tại (nếu có login thì dùng UID thật)
    private String getCurrentUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return DEFAULT_UID;
        }
    }

    // Lưu dữ liệu thời tiết lên Firestore
    public void saveWeatherData(@NonNull WeatherEntity weather, @NonNull WeatherDao weatherDao) {
        Map<String, Object> data = new HashMap<>();
        data.put("city", weather.city);
        data.put("temperature", weather.temperature);
        data.put("latitude", weather.latitude);
        data.put("longitude", weather.longitude);
        data.put("timestamp", weather.timestamp);

        String uid = getCurrentUid();

        db.collection("weatherLogs")
                .document(uid)
                .collection("entries")
                .document(String.valueOf(weather.timestamp))
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Weather saved to Firestore.");

                    // Lưu vào Room bằng Thread riêng
                    new Thread(() -> {
                        try {
                            List<WeatherEntity> list = new ArrayList<>();
                            list.add(weather);
                            weatherDao.insertAll(list);
                            Log.d(TAG, "Weather also saved to Room.");
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to save to Room.", e);
                        }
                    }).start();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save weather data to Firestore.", e));
    }

    // Tải dữ liệu từ Firestore và lưu vào Room
    public void syncWeatherDataFromCloud(@NonNull WeatherDao weatherDao) {
        String uid = getCurrentUid();

        db.collection("weatherLogs")
                .document(uid)
                .collection("entries")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<WeatherEntity> weatherList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        WeatherEntity weather = new WeatherEntity();
                        weather.city = doc.getString("city");
                        Double temp = doc.getDouble("temperature");
                        Double lat = doc.getDouble("latitude");
                        Double lon = doc.getDouble("longitude");
                        Long time = doc.getLong("timestamp");

                        if (weather.city != null && temp != null && lat != null && lon != null && time != null) {
                            weather.temperature = temp;
                            weather.latitude = lat;
                            weather.longitude = lon;
                            weather.timestamp = time;
                            weatherList.add(weather);
                        }
                    }

                    // Lưu vào Room bằng Thread để tránh block UI
                    new Thread(() -> {
                        try {
                            weatherDao.deleteAll();
                            weatherDao.insertAll(weatherList);
                            Log.d(TAG, "Sync completed with " + weatherList.size() + " records.");
                        } catch (Exception e) {
                            Log.e(TAG, "Room sync failed: ", e);
                        }
                    }).start();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to sync from Firestore.", e));
    }

    // Lấy danh sách thành phố không trùng lặp từ Firestore
    public interface CityListCallback {
        void onCitiesLoaded(List<String> cityNames);
    }
    public void syncLocalToCloud(@NonNull WeatherDao weatherDao) {
        String uid = getCurrentUid();

        new Thread(() -> {
            try {
                List<WeatherEntity> allLocal = weatherDao.getAll();

                db.collection("weatherLogs")
                        .document(uid)
                        .collection("entries")
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            List<String> existingTimestamps = new ArrayList<>();
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                existingTimestamps.add(doc.getId());
                            }

                            for (WeatherEntity weather : allLocal) {
                                String docId = String.valueOf(weather.timestamp);
                                if (!existingTimestamps.contains(docId)) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("city", weather.city);
                                    data.put("temperature", weather.temperature);
                                    data.put("latitude", weather.latitude);
                                    data.put("longitude", weather.longitude);
                                    data.put("timestamp", weather.timestamp);

                                    db.collection("weatherLogs")
                                            .document(uid)
                                            .collection("entries")
                                            .document(docId)
                                            .set(data)
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Synced local weather to Firestore: " + weather.city))
                                            .addOnFailureListener(e -> Log.e(TAG, "Failed to sync local weather to Firestore.", e));
                                }
                            }

                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch Firestore entries before syncing local data.", e));
            } catch (Exception e) {
                Log.e(TAG, "Local to cloud sync failed", e);
            }
        }).start();
    }

    public void getCityList(@NonNull CityListCallback callback) {
        String uid = getCurrentUid();

        db.collection("weatherLogs")
                .document(uid)
                .collection("entries")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<String> cities = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        String city = doc.getString("city");
                        if (city != null && !cities.contains(city)) {
                            cities.add(city);
                        }
                    }
                    callback.onCitiesLoaded(cities);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load city list.", e));
    }
}
