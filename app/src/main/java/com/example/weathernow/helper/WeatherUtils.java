package com.example.weathernow.helper;
import com.example.weathernow.R;
public class WeatherUtils {

    // chuẩn hóa mô tả thời tiết từ các ngôn ngữ khác nhau sang tiếng anh
    public static String mapLocalizedDescription(String description) {
        if (description == null || description.isEmpty()) {
            return "cloudy"; // Default
        }

        description = description.toLowerCase();

        // French mappings
        if (description.contains("ciel dégagé") || description.contains("clair")) {
            return "clear sky";
        } else if (description.contains("nuageux") && description.contains("nuit")) {
            return "cloudy night";
        } else if (description.contains("nuageux") || description.contains("nuages")) {
            return "cloudy";
        } else if (description.contains("pluie") && description.contains("nuit")) {
            return "rain night";
        } else if (description.contains("pluie") && description.contains("orage")) {
            return "rain storm";
        } else if (description.contains("pluie")) {
            return "rain";
        } else if (description.contains("tonnerre")) {
            return "thunder";
        } else if (description.contains("soleil") && description.contains("nuage")) {
            return "sun cloud";
        } else if (description.contains("pleine lune")) {
            return "full moon";
        } else if (description.contains("nuit")) {
            return "night";
        }

        // Vietnamese mappings
        if (description.contains("bầu trời quang đãng") || description.contains("trong xanh")) {
            return "clear sky";
        } else if (description.contains("mây") && description.contains("đêm")) {
            return "cloudy night";
        } else if (description.contains("mây")) {
            return "cloudy";
        } else if (description.contains("mưa") && description.contains("đêm")) {
            return "rain night";
        } else if (description.contains("mưa") && description.contains("bão")) {
            return "rain storm";
        } else if (description.contains("mưa")) {
            return "rain";
        } else if (description.contains("sấm sét")) {
            return "thunder";
        } else if (description.contains("nắng") && description.contains("mây")) {
            return "sun cloud";
        } else if (description.contains("trăng tròn")) {
            return "full moon";
        } else if (description.contains("đêm")) {
            return "night";
        }

        // Default to English mappings
        return description;
    }
    // Lấy icon thời tiết dựa trên mô tả
    public static int getWeatherIcon(String description) {
        if (description.contains("clear") && description.contains("night")) {
            return R.drawable.ic_nigh_clear;
        } else if (description.contains("clear")) {
            return R.drawable.ic_sunny;
        } else if (description.contains("cloudy") && description.contains("night")) {
            return R.drawable.ic_night_cloudy;
        } else if (description.contains("cloudy")) {
            return R.drawable.ic_cloudy;
        } else if (description.contains("rain") && description.contains("night")) {
            return R.drawable.ic_night_rain;
        } else if (description.contains("rain") && description.contains("storm")) {
            return R.drawable.ic_heavyrain_storm;
        } else if (description.contains("rain")) {
            return R.drawable.ic_rainy;
        } else if (description.contains("thunder")) {
            return R.drawable.ic_thunder;
        } else if (description.contains("sun") && description.contains("cloud")) {
            return R.drawable.ic_sunny_cloud;
        } else if (description.contains("full moon")) {
            return R.drawable.ic_fullmoon;
        } else if (description.contains("night")) {
            return R.drawable.ic_night;
        } else {
            return R.drawable.ic_cloudy;
        }
    }
}