package com.example.demo.NetworkUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

public class ImageUtils {

    private ImageCache imageCache;

    public ImageUtils() {
        imageCache = new ImageCache();  // 创建一个缓存对象
    }

    // 从Base64字符串设置图片并缓存
    public void setImageFromBase64(String base64String, String cacheKey, ImageView imageView) {
        Bitmap cachedBitmap = imageCache.getBitmapFromMemCache(cacheKey);

        // 如果缓存中有图片，直接加载
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap);
        } else {
            try {
                // 解码Base64字符串
                byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                // 将解码后的Bitmap设置到ImageView
                imageView.setImageBitmap(decodedBitmap);

                // 将Bitmap加入缓存
                imageCache.addBitmapToMemoryCache(cacheKey, decodedBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
