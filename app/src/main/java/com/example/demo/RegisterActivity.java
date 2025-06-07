package com.example.demo;

import static com.example.demo.PasswordStrength.STRONG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.demo.NetworkUtils.ApiService;
import com.example.demo.NetworkUtils.RetrofitClient;
import com.example.demo.NetworkUtils.ServerConfig;
import com.example.demo.NetworkUtils.UserCheck;
import com.example.demo.NetworkUtils.UserInfo;
import com.example.demo.NetworkUtils.UserRegister;
import com.google.gson.GsonBuilder;

import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity implements TextWatcher {

    private EditText registeruser;
    private EditText registerpassword;
    private EditText registerpassword1;
    private ImageView registershowhidepasswordbutton;
    private ImageView registershowhidepasswordbutton1;
    private boolean isPasswordVisible = false;
    private boolean isPasswordVisible1 = false;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);


        mSharedPreferences = getSharedPreferences("user",MODE_PRIVATE);

        //控件
        registeruser = findViewById(R.id.registeruser);
        registerpassword = findViewById(R.id.registerpassword);
        registerpassword1 = findViewById(R.id.registerpassword1);
        registershowhidepasswordbutton = findViewById(R.id.registershowhidepasswordbutton);
        registershowhidepasswordbutton1 = findViewById(R.id.registershowhidepasswordbutton1);
        registerpassword.addTextChangedListener(this);
        //返回登录
        findViewById(R.id.backtologin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //销毁返回
                finish();
            }
        });


        //点击注册
        findViewById(R.id.registerbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = registeruser.getText().toString();
                String password = registerpassword.getText().toString();
                String password1 = registerpassword1.getText().toString();

                if(TextUtils.isEmpty(username) | TextUtils.isEmpty(password) | TextUtils.isEmpty(password1)){
                    Toast.makeText(RegisterActivity.this, "请输入用户名或密码！", Toast.LENGTH_SHORT).show();
                }else{
                    if(!password.equals(password1)){
                        Toast.makeText(RegisterActivity.this, "两次密码输入不匹配！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(PasswordStrength.calculateStrength(password) != STRONG){
                        Toast.makeText(RegisterActivity.this, "*密码由数字、字母或符号至少两种组成的8~20位半角字符", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ApiService service = RetrofitClient.getClient(ServerConfig.serverurl).create(ApiService.class);
                    UserRegister registerRequest = new UserRegister(username, password);

                    Call<UserInfo> call = service.register(registerRequest);

                    call.enqueue(new Callback<UserInfo>() {
                        @Override
                        public void onResponse(@NonNull Call<UserInfo> call, @NonNull Response<UserInfo> response) {
                            if (response.isSuccessful()) {
                                UserInfo user = response.body();
                                Log.e("register", "Error code: " + response.body());
                                assert user != null;
                                if(!Objects.equals(user.getId(), -1)){
                                    Toast.makeText(RegisterActivity.this, "注册成功，请登录！", Toast.LENGTH_SHORT).show();
                                    finish();
                                }else {
                                    Toast.makeText(RegisterActivity.this, "该用户名账户已存在！", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // 处理 HTTP 错误（如 404）
                                Log.e("API", "Error code: " + response.code());
                                Toast.makeText(RegisterActivity.this, "服务器请求错误！", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {
                            // 处理网络异常（如超时）
                            Log.e("API", "Request failed: " + t.getMessage());
                            Toast.makeText(RegisterActivity.this, "服务器不可用！", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        findViewById(R.id.registershowhidepasswordbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });
        findViewById(R.id.registershowhidepasswordbutton1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility1();
            }
        });
    }
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // 如果密码可见，设置为不可见
            registerpassword.setInputType(129);
            registershowhidepasswordbutton.setImageResource(R.drawable.ic_eyeoff);
        } else {
            // 如果密码不可见，设置为可见
            registerpassword.setInputType(145);
            registershowhidepasswordbutton.setImageResource(R.drawable.ic_eyeon);
        }
        isPasswordVisible = !isPasswordVisible;
        registerpassword.setSelection(registerpassword.getText().length());
    };
    private void togglePasswordVisibility1() {
        if (isPasswordVisible1) {
            // 如果密码可见，设置为不可见
            registerpassword1.setInputType(129);
            registershowhidepasswordbutton1.setImageResource(R.drawable.ic_eyeoff); // 显示"眼睛"图标
        } else {
            // 如果密码不可见，设置为可见
            registerpassword1.setInputType(145);
            registershowhidepasswordbutton1.setImageResource(R.drawable.ic_eyeon); // 显示"取消眼睛"图标
        }
        isPasswordVisible1 = !isPasswordVisible1;
        registerpassword1.setSelection(registerpassword1.getText().length());
    };

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    public void afterTextChanged(Editable editable) {
        updatePasswordStrengthView(editable.toString());
    }
    private void updatePasswordStrengthView(String password) {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_changepassword);
        TextView strengthView = (TextView) findViewById(R.id.password_strength_change);

        if (TextView.VISIBLE != strengthView.getVisibility())
            return;

        if (password.isEmpty()) {
            strengthView.setText("");
            progressBar.setProgress(0);
            return;
        }

        PasswordStrength str = PasswordStrength.calculateStrength(password);
        strengthView.setText(str.getText(this));
        strengthView.setTextColor(str.getColor());

        progressBar.getProgressDrawable().setColorFilter(str.getColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        if (str.getText(this).equals("低")) {
            progressBar.setProgress(33);
        } else if (str.getText(this).equals("中")) {
            progressBar.setProgress(66);
        } else if (str.getText(this).equals("高")) {
            progressBar.setProgress(100);
        } else {
            progressBar.setProgress(0);
        }
    }
}