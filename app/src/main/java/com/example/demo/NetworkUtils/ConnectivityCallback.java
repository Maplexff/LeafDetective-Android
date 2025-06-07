package com.example.demo.NetworkUtils;

public interface ConnectivityCallback {
    void onResult(boolean isConnected);
    void onError(String errorMessage);
}