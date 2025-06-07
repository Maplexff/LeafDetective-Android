package com.example.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.demo.NetworkUtils.ApiService;
import com.example.demo.NetworkUtils.ConnectivityCallback;
import com.example.demo.NetworkUtils.RetrofitClient;
import com.example.demo.NetworkUtils.ServerConfig;
import com.example.demo.NetworkUtils.UserCheck;
import com.example.demo.NetworkUtils.UserRegister;
import com.example.demo.ui.home.HomeViewModel;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private EditText loginuser;
    private EditText loginpassword;
    private CheckBox checkbox;
    private EditText loginurl;
    private SharedPreferences mSharedPreferences;

    private Boolean is_login;
    private Boolean is_rememberpwd;
    private ImageView showhidepasswordbutton;





    private boolean isPasswordVisible = false;

//    public static void set_SharedPreferences_s_url(String url)
//    {
//        mSharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
//        SharedPreferences.Editor edit = mSharedPreferences.edit();
//        edit.putString("s_url",url);
//        edit.apply();
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        //控件
        loginuser = findViewById(R.id.loginuser);
        loginpassword = findViewById(R.id.loginpassword);
        checkbox = findViewById(R.id.rememberpwd);
        loginurl = findViewById(R.id.loginurlcontent);
        showhidepasswordbutton = findViewById(R.id.showhidepasswordbutton);




        mSharedPreferences = getSharedPreferences("user",MODE_PRIVATE);

        String s_url = mSharedPreferences.getString("serverurl",null);
        if(s_url != null){
            ServerConfig.serverurl = s_url;
            loginurl.setHint(s_url);
        }else{
            loginurl.setHint(ServerConfig.serverurl);
        }
        //已登录
        is_login = mSharedPreferences.getBoolean("is_login",false);

//        is_login=true;

        if(is_login){
            String name = mSharedPreferences.getString("name",null);
            String pwd = mSharedPreferences.getString("password",null);
            String id = mSharedPreferences.getString("id",null);
            loginuser.setText(name);
            loginpassword.setText(pwd);
            checkbox.setChecked(true);

            Intent intent = new Intent(LoginActivity.this,MainDrawerActivity.class);
            startActivity(intent);
            finish();
        }


        //记住密码
        is_rememberpwd = mSharedPreferences.getBoolean("is_rememberpwd",false);
        if(is_rememberpwd){
            String name = mSharedPreferences.getString("name",null);
            String pwd = mSharedPreferences.getString("password",null);
            loginuser.setText(name);
            loginpassword.setText(pwd);
            checkbox.setChecked(true);
        }




        //点击登录
        findViewById(R.id.loginbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = loginuser.getText().toString();
                String password = loginpassword.getText().toString();

                if(TextUtils.isEmpty(username) | TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this, "请输入用户名或密码！", Toast.LENGTH_SHORT).show();
                }else{

                    ApiService service = RetrofitClient.getClient(ServerConfig.serverurl).create(ApiService.class);

                    // 创建 LoginRequest 对象
                    UserRegister loginRequest = new UserRegister(username, password);

                    Call<UserCheck> call = service.login(loginRequest);

                    call.enqueue(new Callback<UserCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<UserCheck> call, @NonNull Response<UserCheck> response) {
                            if (response.isSuccessful()) {
                                UserCheck user = response.body();
                                // 更新 UI（需切回主线程）
//                                runOnUiThread(() -> textView.setText(user.getName()));
                                if(user.getCheckinfo()){
                                    is_login = true;
                                    SharedPreferences.Editor edit = mSharedPreferences.edit();
                                    edit.putBoolean("is_login",is_login);
                                    edit.putBoolean("is_rememberpwd",is_rememberpwd);
                                    edit.putString("name",username);
                                    edit.putString("password",password);
                                    edit.putString("id",Integer.toString(user.getId()));
                                    edit.commit();


//                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                    Intent intent = new Intent(LoginActivity.this,MainDrawerActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    Toast.makeText(LoginActivity.this, "用户名或密码错误！", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // 处理 HTTP 错误（如 404）
                                Log.e("Login", "Error code: " + response.code());
                                Toast.makeText(LoginActivity.this, "服务器请求错误！", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<UserCheck> call, @NonNull Throwable t) {
                            // 处理网络异常（如超时）
                            Log.e("API", "Request failed: " + t.getMessage());
                            Toast.makeText(LoginActivity.this, "服务器不可用！", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });


        findViewById(R.id.showhidepasswordbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        //注册
        findViewById(R.id.notregister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转注册
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                is_rememberpwd = isChecked;
            }
        });


        findViewById(R.id.loginurlbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = loginurl.getText().toString();
                if(!url.isEmpty()){
                    if(ServerConfig.isHttpOrHttps(url)){
                        // 在 UI 组件中调用
                        ServerConfig.checkServerConnectivity(url,new ConnectivityCallback() {
                            @Override
                            public void onResult(boolean isConnected) {
                                runOnUiThread(() -> {
                                    if (isConnected) {
                                        Toast.makeText(LoginActivity.this, "服务器在线！", Toast.LENGTH_SHORT).show();
                                        ServerConfig.setServerurl(LoginActivity.this,url);
                                        HomeViewModel.setHint(ServerConfig.serverurl);
                                        loginurl.setHint(ServerConfig.serverurl);
                                        SharedPreferences.Editor edit = mSharedPreferences.edit();
                                        edit.putString("serverurl",url);
                                        edit.commit();

                                    } else {
                                        Toast.makeText(LoginActivity.this, "服务器不在线,请检查输入！", Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                            @Override
                            public void onError(String errorMessage) {
                                runOnUiThread(() ->
                                        Log.e("NetworkCheck", "检测失败: " + errorMessage)
                                );
                                Toast.makeText(LoginActivity.this, "服务器不在线,请检查输入！", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }else{
                        Toast.makeText(LoginActivity.this, "设置url失败，请检查输入！", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(LoginActivity.this, "Url输入为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
    // 切换密码可见性
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // 如果密码可见，设置为不可见
            loginpassword.setInputType(129); // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            showhidepasswordbutton.setImageResource(R.drawable.ic_eyeon); // 显示"眼睛"图标
        } else {
            // 如果密码不可见，设置为可见
            loginpassword.setInputType(145); // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            showhidepasswordbutton.setImageResource(R.drawable.ic_eyeoff); // 显示"取消眼睛"图标
        }

        // 切换密码可见性的状态
        isPasswordVisible = !isPasswordVisible;

        // 将光标移动到末尾，确保输入的位置保持不变
        loginpassword.setSelection(loginpassword.getText().length());
    }
}