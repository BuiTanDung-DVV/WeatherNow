package com.example.weathernow.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.weathernow.data.WeatherDao;
import com.example.weathernow.data.WeatherEntity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FirestoreManager {

    private static final String TAG = "FirestoreManager";
    private static final String COLLECTION_NAME = "weatherLogs";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private final FirebaseFirestore db;

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    // Convert timestamp (long) to formatted string
    private String formatTimestamp(long timestamp) {
        return sdf.format(new Date(timestamp));
    }

    // Convert formatted string to timestamp (long)
    private long parseTimestamp(String formatted) {
        try {
            Date date = sdf.parse(formatted);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            Log.e(TAG, "Lỗi chuyển đổi thời gian từ chuỗi", e);
            return 0;
        }
    }

    // Chuyển WeatherEntity thành Map<String, Object>
    private Map<String, Object> weatherToMap(WeatherEntity weather) {
        Map<String, Object> data = new HashMap<>();
        data.put("city", weather.getCity());
        data.put("temperature", weather.getTemperature());
        data.put("latitude", weather.getLatitude());
        data.put("longitude", weather.getLongitude());
        data.put("timestamp", formatTimestamp(weather.getTimestamp()));
        data.put("description", weather.getDescription());
        data.put("humidity", weather.getHumidity());
        data.put("windSpeed", weather.getWindSpeed());
        return data;
    }

    // Lưu dữ liệu thời tiết lên Firestore
    public void saveWeatherData(@NonNull WeatherEntity weather, @NonNull WeatherDao weatherDao) {
        Map<String, Object> data = weatherToMap(weather);
        String docId = weather.getCity() + "_" + weather.getTimestamp();

        db.collection(COLLECTION_NAME)
                .document(docId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Đã lưu dữ liệu thời tiết lên Firestore.");
                    new Thread(() -> {
                        try {
                            weatherDao.insertAll(Collections.singletonList(weather));
                            Log.d(TAG, "Đã lưu dữ liệu thời tiết vào Room.");
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi lưu vào Room.", e);
                        }
                    }).start();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Không thể lưu dữ liệu thời tiết lên Firestore.", e));
    }

    // Tải dữ liệu từ Firestore và lưu vào Room
    public void syncWeatherDataFromCloud(@NonNull WeatherDao weatherDao) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<WeatherEntity> weatherList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        try {
                            WeatherEntity weather = new WeatherEntity();
                            weather.setCity(doc.getString("city"));
                            weather.setTemperature(doc.getDouble("temperature") != null ? doc.getDouble("temperature") : 0);
                            weather.setLatitude(doc.getDouble("latitude") != null ? doc.getDouble("latitude") : 0);
                            weather.setLongitude(doc.getDouble("longitude") != null ? doc.getDouble("longitude") : 0);
                            weather.setTimestamp(parseTimestamp(doc.getString("timestamp")));
                            weather.setDescription(doc.getString("description"));
                            weather.setHumidity(doc.getDouble("humidity") != null ? doc.getDouble("humidity").intValue() : 0);
                            weather.setWindSpeed(doc.getDouble("windSpeed") != null ? doc.getDouble("windSpeed") : 0);
                            weatherList.add(weather);
                        } catch (Exception e) {
                            Log.e(TAG, "Bỏ qua một bản ghi không hợp lệ.", e);
                        }
                    }

                    new Thread(() -> {
                        try {
                            weatherDao.insertAll(weatherList);
                            Log.d(TAG, "Đã đồng bộ từ Firestore vào Room: " + weatherList.size() + " bản ghi.");
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi lưu dữ liệu từ Firestore vào Room.", e);
                        }
                    }).start();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi khi truy xuất dữ liệu từ Firestore.", e));
    }

    // Lấy danh sách các thành phố không trùng lặp
    public interface CityListCallback {
        void onCitiesLoaded(List<String> cityNames);
    }

    public void getCityList(@NonNull CityListCallback callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(snapshots -> {
                    Set<String> citySet = new HashSet<>();
                    for (DocumentSnapshot doc : snapshots) {
                        String city = doc.getString("city");
                        if (city != null) {
                            citySet.add(city);
                        }
                    }
                    callback.onCitiesLoaded(new ArrayList<>(citySet));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Không thể tải danh sách thành phố.", e));
    }
}