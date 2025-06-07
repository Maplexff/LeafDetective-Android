package com.example.demo.NetworkUtils;

import android.util.Log;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;



public class RetrofitClient {
//    private static final String BASE_URL = ServerConfig.serverurl;
    private static final int TIMEOUT = 3; // 秒

    private static Retrofit retrofitInstance;
    public static synchronized Retrofit getClient(String Input_URL) {
        if(!Objects.equals(Input_URL, ServerConfig.serverurl)){
            retrofitInstance = null;
        }
        if (retrofitInstance == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                    .build();

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(Input_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitInstance;
    }
    public static synchronized void setClient(String Input_URL) {
            retrofitInstance = null;
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                    .build();

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(Input_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

    }

}

