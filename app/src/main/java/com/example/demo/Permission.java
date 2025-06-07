package com.example.demo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Permission {
    // 常用权限定义
//    public static final String CAMERA_PERMISSION = android.Manifest.permission.CAMERA;
    //定义权限
    public static final String[] ALL_PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.FOREGROUND_SERVICE,
//            Manifest.permission.ACCESS_BACKGROUND_LOCATION

    };
    public static final String[] STORAGE_PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final String[] shot_PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    public static final String[] location_PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            android.Manifest.permission.FOREGROUND_SERVICE,
//            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION

    };
    public static final String[] internet_PERMISSIONS = {
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.CHANGE_WIFI_STATE,
    };
    public interface PermissionResultCallback {
        void onAllGranted();
        void onDenied(List<String> deniedPermissions);
    }

    /**
     * 发起权限请求
     */
    public static void requestPermissions(
            Context context,
            String[] permissions,
            ActivityResultLauncher<String[]> launcher,
            PermissionResultCallback callback
    ) {
        // 检查是否已拥有所有权限
        if (checkAllPermissionsGranted(context, permissions)) {
            callback.onAllGranted();
            return;
        }

        launcher.launch(permissions);
    }

    /**
     * 处理权限请求结果
     */
    public static void handlePermissionResult(
            String[] requestedPermissions,
            Map<String, Boolean> results,
            PermissionResultCallback callback
    ) {
        List<String> deniedList = new ArrayList<>();
        for (String permission : requestedPermissions) {
            Boolean result = results.get(permission);
            if (result == null || !result) {
                deniedList.add(permission);
            }
        }

        if (deniedList.isEmpty()) {
            callback.onAllGranted();
        } else {
            callback.onDenied(deniedList);
        }
    }

    /**
     * 检查所有权限是否已授予
     */
    public static boolean checkAllPermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
