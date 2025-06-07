package com.example.demo.NetworkUtils;

import android.graphics.Bitmap;
import android.util.LruCache;

public class ImageCache {
    private LruCache<String, Bitmap> mMemoryCache;

    public ImageCache() {

        // 获取可用内存大小
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // 设置缓存的最大大小为可用内存的1/8
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024; // 返回图片的大小（以KB为单位）
            }
        };
    }

    // 添加图片到缓存
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    // 从缓存中获取图片
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
    public void clearCache() {
        mMemoryCache.evictAll();
    }
}
