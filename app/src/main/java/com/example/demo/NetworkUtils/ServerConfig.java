package com.example.demo.NetworkUtils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;



public class ServerConfig {
    public static String serverurl = "http://192.168.31.178:9099" ;
    private static final int TIMEOUT_SECONDS = 5;

    public static void setServerurl(Context context,String serverurl) {

//        if(isHttpOrHttps(serverurl)){
            ServerConfig.serverurl = serverurl;
//            Toast.makeText(ServerConfig.requireActivity(context), "设置url:" + ServerConfig.serverurl, Toast.LENGTH_SHORT).show();
//        }else{
//            Toast.makeText(ServerConfig.requireActivity(context), "设置url失败，请检查输入！" + ServerConfig.serverurl, Toast.LENGTH_SHORT).show();
//        }
    }

    private static Context requireActivity(Context context) {
        return context;
    }
    public static boolean isHttpOrHttps(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

     //执行异步检测
    public static void checkServerConnectivity(String Input_URL,ConnectivityCallback callback) {

        ApiService service = RetrofitClient.getClient(Input_URL).create(ApiService.class);
        service.checkServerStatus().enqueue(new Callback<CheckResponse>() {
            @Override
            public void onResponse(Call<CheckResponse> call, Response<CheckResponse> response) {
                // 2xx 状态码视为成功
                if (response.isSuccessful() && response.body() != null) {
                    // 第二层：业务状态检测
                    boolean isAlive = "success".equals(response.body().getStatus());
                    callback.onResult(isAlive);
                } else {
                    RetrofitClient.setClient(ServerConfig.serverurl);
                    callback.onError("检测异常: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CheckResponse> call, Throwable t) {
                RetrofitClient.setClient(ServerConfig.serverurl);
                callback.onError(t.getMessage());
            }
        });
    }

}
