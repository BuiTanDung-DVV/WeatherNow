package com.example.weathernow.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LocationHelper {

    private final FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    // Phương thức yêu cầu lấy vị trí và thông báo kết quả qua listener
    public void getLastLocation(final Context context, final LocationListener listener) {
        // Kiểm tra quyền truy cập vị trí
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa có quyền, yêu cầu quyền
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Lấy vị trí cuối cùng của người dùng
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener((Activity) context, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location != null) {
                            // Nếu có tọa độ, gọi listener với lat và lon
                            listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                        } else {
                            Toast.makeText(context, "Không thể lấy vị trí", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Interface để nhận tọa độ
    public interface LocationListener {
        void onLocationReceived(double latitude, double longitude);
    }
}
