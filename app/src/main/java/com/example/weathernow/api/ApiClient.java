package com.example.weathernow.api;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            final String apiKey = ApiKeyProvider.getApiKey(context);

            Interceptor apiKeyInterceptor = new Interceptor() {
                @NonNull
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();
                    HttpUrl originalUrl = originalRequest.url();

                    // Add API key as a query parameter in the request
                    HttpUrl newUrl = originalUrl.newBuilder()
                            .addQueryParameter("appid", apiKey)
                            .build();

                    Request newRequest = originalRequest.newBuilder()
                            .url(newUrl)
                            .build();

                    return chain.proceed(newRequest);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(apiKeyInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}