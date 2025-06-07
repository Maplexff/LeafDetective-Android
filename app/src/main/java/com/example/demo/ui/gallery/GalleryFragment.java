package com.example.demo.ui.gallery;

import static android.content.Context.MODE_PRIVATE;

import static com.example.demo.PasswordStrength.STRONG;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.demo.LoginActivity;
import com.example.demo.NetworkUtils.ApiService;
import com.example.demo.NetworkUtils.RetrofitClient;
import com.example.demo.NetworkUtils.ServerConfig;
import com.example.demo.NetworkUtils.UserCheck;
import com.example.demo.NetworkUtils.UserInfo;
import com.example.demo.NetworkUtils.UserPwd;
import com.example.demo.PasswordStrength;
import com.example.demo.R;
import com.example.demo.databinding.ActivityMainDrawerBinding;
import com.example.demo.databinding.FragmentGalleryBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private ActivityMainDrawerBinding binding1;
    private SharedPreferences mSharedPreferences;
    private String id = null;
    private String name = null;
    private String password = null;
    private TextView idtext;
    private TextView nametext;
    private TextView passwordtext;
    private ImageView passwordvis;
    private TextView baridtext;
    private TextView barnametext;
    private boolean isPasswordVisible = false;  // 默认密码不可见
    private boolean isPasswordVisible1 = false;  // 默认密码不可见
    private boolean isPasswordVisible2 = false;  // 默认密码不可见

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);
        mSharedPreferences = requireActivity().getSharedPreferences("user",MODE_PRIVATE);
        id = mSharedPreferences.getString("id",null);
        name = mSharedPreferences.getString("name",null);
        password = mSharedPreferences.getString("password",null);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        idtext = root.findViewById(R.id.idshow);
        nametext = root.findViewById(R.id.nameshow);
        passwordtext = root.findViewById(R.id.passwordshow);
        passwordvis = root.findViewById(R.id.pwdshowhidepasswordbutton);

        idtext.setText(id);
        nametext.setText(name);
        passwordtext.setText(password);


//        binding.appBarMainDrawer.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null)
//                        .setAnchorView(R.id.fab).show();
//            }
//        });


        assert getActivity() != null;
        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        // 获取头部视图
        View headerView = navigationView.getHeaderView(0);

        baridtext = headerView.findViewById(R.id.idtextView);
        barnametext = headerView.findViewById(R.id.nametextView);

        SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                // 检查改变的数据是哪一个（"id" 或 "name"）
                assert key != null;
                if (key.equals("name")) {
                    // 从 SharedPreferences 获取最新的 id 和 name
                    String name = mSharedPreferences.getString("name", null);
//                    Log.d("PreferenceChange", "id: " + id + ", name: " + name);
                    // 更新 UI
                    if (id != null && name != null) {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 更新 UI
                                barnametext.setText(String.format(getResources().getString(R.string.username_label), name));
                            }
                        });
                    }
                }
            }
        };

// 注册监听器
//        mSharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        root.findViewById(R.id.nologinchangeshow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor edit = mSharedPreferences.edit();
                edit.putBoolean("is_login",false);
                edit.commit();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish(); // 销毁宿主Activity
                };
            }
        });

        root.findViewById(R.id.ic_nameedit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialogName("更改用户名", null);
            }
        });
        root.findViewById(R.id.ic_passwordedit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialogPwd("更改密码", null);
            }
        });
        root.findViewById(R.id.pwdshowhidepasswordbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // 如果密码可见，设置为不可见
                    passwordtext.setInputType(129); // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    passwordvis.setImageResource(R.drawable.ic_eyeoff); // 显示"眼睛"图标
                } else {
                    // 如果密码不可见，设置为可见
                    passwordtext.setInputType(145);
                    passwordvis.setImageResource(R.drawable.ic_eyeon); // 显示"取消眼睛"图标
                }
                // 切换密码可见性的状态
                isPasswordVisible = !isPasswordVisible;
            }
        });

        return root;
    }

    private void showEditDialogName(String title, String currentValue) {

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.mydialogtheme, null);

        TextView mydialogtitle = dialogView.findViewById(R.id.my_dialog_title);
        EditText mydialoginput = dialogView.findViewById(R.id.my_dialog_input);

        mydialogtitle.setText(title);
        mydialoginput.setText(currentValue);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        dialog.setContentView(dialogView);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        dialog.setOnShowListener(d -> {
//            Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button btnPositive = dialog.findViewById(R.id.positivebtn);
            Button btnNegative = dialog.findViewById(R.id.negativebtn);
            btnPositive.setOnClickListener(v -> {
                String newValue = mydialoginput.getText().toString();
                if (newValue.isEmpty()) {
                    mydialoginput.setError("输入不能为空！");
                    return;
                }
                ApiService service = RetrofitClient.getClient(ServerConfig.serverurl).create(ApiService.class);
                // 创建 LoginRequest 对象
                UserInfo editnamebyidRequest = new UserInfo(Integer.parseInt(id), newValue);

                Call<UserCheck> call = service.editnamebyid(editnamebyidRequest);

                call.enqueue(new Callback<UserCheck>() {
                    @Override
                    public void onResponse(@NonNull Call<UserCheck> call, @NonNull Response<UserCheck> response) {
                        if (response.isSuccessful()) {
                            UserCheck user = response.body();
                            assert user != null;
                            if(user.getCheckinfo()){
                                SharedPreferences.Editor edit = mSharedPreferences.edit();
                                edit.putString("name",newValue);
                                edit.commit();
                                Toast.makeText(requireActivity(), "ID:" + user.getId() + "用户名更改成功！", Toast.LENGTH_SHORT).show();
                                nametext.setText(newValue);
//                                barnametext.setText(String.format(getResources().getString(R.string.username_label), name));
                                dialog.dismiss();
                            }else {
                                Toast.makeText(requireActivity(), "用户名更改错误，已存在同名用户！ID:" + user.getId(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // 处理 HTTP 错误（如 404）
                            Log.e("Edit", "Error code: " + response.code());
                            Toast.makeText(requireActivity(), "服务器请求错误！", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserCheck> call, @NonNull Throwable t) {
                        // 处理网络异常（如超时）
                        Log.e("API", "Request failed: " + t.getMessage());
                        Toast.makeText(requireActivity(), "服务器不可用！", Toast.LENGTH_SHORT).show();
                    }
                });

//                } else {
//                    ApiService service = RetrofitClient.getClient(ServerConfig.serverurl).create(ApiService.class);
//                    // 创建 LoginRequest 对象
//                    UserPwd editpwdbyidRequest = new UserPwd(Integer.parseInt(id), newValue);
//
//                    Call<UserCheck> call = service.editpwdbyid(editpwdbyidRequest);
//
//                    call.enqueue(new Callback<UserCheck>() {
//                        @Override
//                        public void onResponse(@NonNull Call<UserCheck> call, @NonNull Response<UserCheck> response) {
//                            if (response.isSuccessful()) {
//                                UserCheck user = response.body();
//                                assert user != null;
//                                if(user.getCheckinfo()){
//                                    SharedPreferences.Editor edit = mSharedPreferences.edit();
//                                    edit.putString("password",newValue);
//                                    edit.commit();
//                                    Toast.makeText(requireActivity(), "ID:" + user.getId() + "密码更改成功！", Toast.LENGTH_SHORT).show();
//                                    passwordtext.setText(newValue);
//                                    dialog.dismiss();
//                                }else {
//                                    Toast.makeText(requireActivity(), "密码更改错误！", Toast.LENGTH_SHORT).show();
//                                }
//                            } else {
//                                // 处理 HTTP 错误（如 404）
//                                Log.e("Edit", "Error code: " + response.code());
//                                Toast.makeText(requireActivity(), "服务器请求错误！", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(@NonNull Call<UserCheck> call, @NonNull Throwable t) {
//                            // 处理网络异常（如超时）
//                            Log.e("API", "Request failed: " + t.getMessage());
//                            Toast.makeText(requireActivity(), "服务器不可用！", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//
//                }
//                dialog.dismiss();

            });
            btnNegative.setOnClickListener(v -> {
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void showEditDialogPwd(String title, String currentValue) {

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.mydialogpassword, null);

        TextView mydialogtitle = dialogView.findViewById(R.id.my_dialog_title);
        EditText mydialoginput1 = dialogView.findViewById(R.id.my_dialog_inputpwd1);
        EditText mydialoginput2 = dialogView.findViewById(R.id.my_dialog_inputpwd2);

        mydialogtitle.setText(title);
        mydialoginput1.setText(currentValue);
        mydialoginput2.setText(currentValue);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        dialog.setContentView(dialogView);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        dialog.setOnShowListener(d -> {
//            Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button btnPositive = dialog.findViewById(R.id.positivebtn);
            Button btnNegative = dialog.findViewById(R.id.negativebtn);
            ImageView showbtn1 = dialog.findViewById(R.id.cpwdshowhidepasswordbutton1);
            ImageView showbtn2 = dialog.findViewById(R.id.cpwdshowhidepasswordbutton2);

            btnPositive.setOnClickListener(v -> {
                String newValue = mydialoginput1.getText().toString();
                String newValue1 = mydialoginput2.getText().toString();
                if (newValue.isEmpty()) {
                    mydialoginput1.setError("输入不能为空！");
                    return;
                }
                if (newValue1.isEmpty()) {
                    mydialoginput2.setError("输入不能为空！");
                    return;
                }

                if(!newValue.equals(newValue1)){
                    mydialoginput2.setError("两次输入不匹配！");
                    return;
                }
                if(PasswordStrength.calculateStrength(newValue) != STRONG){
                    mydialoginput1.setError("*密码由数字、字母或符号至少两种组成的8~20位半角字符");
                    return;
                }

                ApiService service = RetrofitClient.getClient(ServerConfig.serverurl).create(ApiService.class);
                // 创建 LoginRequest 对象
                UserPwd editpwdbyidRequest = new UserPwd(Integer.parseInt(id), newValue);
                Call<UserCheck> call = service.editpwdbyid(editpwdbyidRequest);
                call.enqueue(new Callback<UserCheck>() {
                    @Override
                    public void onResponse(@NonNull Call<UserCheck> call, @NonNull Response<UserCheck> response) {
                        if (response.isSuccessful()) {
                            UserCheck user = response.body();
                            assert user != null;
                            if(user.getCheckinfo()){
                                SharedPreferences.Editor edit = mSharedPreferences.edit();
                                edit.putString("password",newValue);
                                edit.commit();
                                Toast.makeText(requireActivity(), "ID:" + user.getId() + "密码更改成功！", Toast.LENGTH_SHORT).show();
                                passwordtext.setText(newValue);
                                dialog.dismiss();
                            }else {
                                Toast.makeText(requireActivity(), "密码更改错误！", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // 处理 HTTP 错误（如 404）
                            Log.e("Edit", "Error code: " + response.code());
                            Toast.makeText(requireActivity(), "服务器请求错误！", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserCheck> call, @NonNull Throwable t) {
                        // 处理网络异常（如超时）
                        Log.e("API", "Request failed: " + t.getMessage());
                        Toast.makeText(requireActivity(), "服务器不可用！", Toast.LENGTH_SHORT).show();
                    }
                });
            });
            btnNegative.setOnClickListener(v -> {
                dialog.dismiss();
            });

            showbtn1.setOnClickListener(v -> {
                if (isPasswordVisible1) {
                    // 如果密码可见，设置为不可见
                    mydialoginput1.setInputType(129); // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    showbtn1.setImageResource(R.drawable.ic_eyeoff); // 显示"眼睛"图标
                } else {
                    // 如果密码不可见，设置为可见
                    mydialoginput1.setInputType(145); // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    showbtn1.setImageResource(R.drawable.ic_eyeon); // 显示"取消眼睛"图标
                }

                // 切换密码可见性的状态
                isPasswordVisible1 = !isPasswordVisible1;

                // 将光标移动到末尾，确保输入的位置保持不变
                mydialoginput1.setSelection(mydialoginput1.getText().length());
            });

            showbtn2.setOnClickListener(v -> {
                if (isPasswordVisible2) {
                    // 如果密码可见，设置为不可见
                    mydialoginput2.setInputType(129); // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    showbtn2.setImageResource(R.drawable.ic_eyeoff); // 显示"眼睛"图标
                } else {
                    // 如果密码不可见，设置为可见
                    mydialoginput2.setInputType(145); // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    showbtn2.setImageResource(R.drawable.ic_eyeon); // 显示"取消眼睛"图标
                }

                // 切换密码可见性的状态
                isPasswordVisible2 = !isPasswordVisible2;

                // 将光标移动到末尾，确保输入的位置保持不变
                mydialoginput2.setSelection(mydialoginput2.getText().length());
            });


        });
        dialog.show();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}