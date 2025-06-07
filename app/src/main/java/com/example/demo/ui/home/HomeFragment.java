package com.example.demo.ui.home;

import static android.content.Context.MODE_PRIVATE;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.demo.NetworkUtils.ApiService;
import com.example.demo.NetworkUtils.ConnectivityCallback;
import com.example.demo.NetworkUtils.RetrofitClient;
import com.example.demo.Permission;
import com.example.demo.R;
import com.example.demo.ResultActivity;
import com.example.demo.databinding.FragmentHomeBinding;
import com.yalantis.ucrop.UCrop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.example.demo.MyWebviewActivity;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.demo.NetworkUtils.ServerConfig;


public class HomeFragment extends Fragment implements AMapLocationListener,GeocodeSearch.OnGeocodeSearchListener{

    private String pred_class;
    private int pred_label;
    private Double pred_score;
    private FragmentHomeBinding binding;
    private EditText searchcontent;
    private EditText urlcontent;
    private TextView idtext;
    private TextView nametext;
    private SharedPreferences mSharedPreferences;
    private Uri imageUri;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private boolean server_status = false;
    private boolean is_camera_falg = false;
    private double lattemp = 360;
    private double lngtemp = 360;
    private String datetimetemp = null;
    private String addresstemp = null;

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    //地理编码搜索
    private GeocodeSearch geocodeSearch;
    //解析成功标识码
    private static final int PARSE_SUCCESS_CODE = 1000;
    //位置更改监听
    private LocationSource.OnLocationChangedListener mListener;
    private boolean need_toast = false;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mSharedPreferences = requireContext().getSharedPreferences("user", MODE_PRIVATE);
        searchcontent = root.findViewById(R.id.searchcontent);
        urlcontent = root.findViewById(R.id.urlcontent);



        initLocation();

//        String user = mSharedPreferences.getString("user", null);

         //观察 LiveData
        HomeViewModel.getHint().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String hintText) {
                // 动态更新 hint 内容
                urlcontent.setHint(hintText);
            }
        });

        HomeViewModel.setHint(ServerConfig.serverurl);




        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                results -> Permission.handlePermissionResult(
                        Permission.ALL_PERMISSIONS,
                        results,
                        new Permission.PermissionResultCallback() {
                            @Override
                            public void onAllGranted() {
//                                startCameraAndStorageOperations();
                                Toast.makeText(requireContext(), "已获取全部必要权限！", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onDenied(List<String> deniedPermissions) {
                                handleDeniedPermissions(deniedPermissions);
//                                Toast.makeText(requireContext(), "未获取全部必要权限！", Toast.LENGTH_SHORT).show();
                            }
                        })
        );

        // 注册拍照的ActivityResultLauncher
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        is_camera_falg = true;
                        mLocationClient.startLocation();

                        startCrop(imageUri);
                    }
                }
        );

        // 注册从相册选择图片的ActivityResultLauncher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        is_camera_falg = false;
                        startCrop(uri);
                    }
                }
        );

        // 注册裁剪图片的ActivityResultLauncher
        cropImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri resultUri = UCrop.getOutput(result.getData());

                        if (resultUri != null) {


                            // 在 UI 组件中调用
                            ServerConfig.checkServerConnectivity(ServerConfig.serverurl,new ConnectivityCallback() {
                                @Override
                                public void onResult(boolean isConnected) {
                                    requireActivity().runOnUiThread(() -> {
                                        if (isConnected) {
//                                            Toast.makeText(requireActivity(), "服务器在线", Toast.LENGTH_SHORT).show();
                                            server_status = true;
                                            uploadImage(resultUri);

                                        } else {
                                            Toast.makeText(requireContext(), "服务器不可达", Toast.LENGTH_SHORT).show();
                                            server_status = false;
                                        }
                                    });
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    requireActivity().runOnUiThread(() ->
                                            Log.e("NetworkCheck", "检测失败: " + errorMessage)

                                    );
                                    server_status = false;
                                    Toast.makeText(requireContext(), "服务器不在线", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                }
        );

        Permission.requestPermissions(
                requireContext(),
                Permission.ALL_PERMISSIONS,
                permissionLauncher,
                new Permission.PermissionResultCallback() {
                    @Override
                    public void onAllGranted() {

//                        Toast.makeText(requireContext(), "已获取全部必要权限！", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onDenied(List<String> deniedPermissions) {
//                        Toast.makeText(requireContext(), "未获取全部必要权限！", Toast.LENGTH_SHORT).show();
                        handleDeniedPermissions(deniedPermissions);
                    }
                }
        );



        root.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Permission.checkAllPermissionsGranted(requireContext(), Permission.ALL_PERMISSIONS))
                {
                    Toast.makeText(requireContext(), "必要权限不足，无法拍摄！", Toast.LENGTH_SHORT).show();
                    retryPermissionRequest();
                    return;
                }
                startCamera();
            }
        });

        root.findViewById(R.id.photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Permission.checkAllPermissionsGranted(requireContext(), Permission.ALL_PERMISSIONS))
                {
                    Toast.makeText(requireContext(), "必要权限不足，无法读取相册！", Toast.LENGTH_SHORT).show();
                    retryPermissionRequest();
                    return;
                }
//                Toast.makeText(requireContext(), "照片", Toast.LENGTH_SHORT).show();
                pickImageLauncher.launch("image/*");

            }
        });

        root.findViewById(R.id.searcherbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search = searchcontent.getText().toString();
//                Toast.makeText(requireContext(), search, Toast.LENGTH_SHORT).show();

                if(!search.isEmpty()){
//                    start(requireContext(), search);
                    Intent intent = new Intent(requireContext(), MyWebviewActivity.class);
                    intent.putExtra("keyword", search);
                    requireContext().startActivity(intent);

                }else{
                    Toast.makeText(requireContext(), "搜索输入为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        root.findViewById(R.id.urlbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = urlcontent.getText().toString();
                if(!url.isEmpty()){
                    if(ServerConfig.isHttpOrHttps(url)){
                        // 在 UI 组件中调用
                        ServerConfig.checkServerConnectivity(url,new ConnectivityCallback() {
                            @Override
                            public void onResult(boolean isConnected) {
                                requireActivity().runOnUiThread(() -> {
                                    if (isConnected) {
                                        Toast.makeText(requireActivity(), "服务器在线！", Toast.LENGTH_SHORT).show();
                                        ServerConfig.setServerurl(requireContext(),url);
                                        HomeViewModel.setHint(ServerConfig.serverurl);
//                                        loginurl.setHint(ServerConfig.serverurl);
                                        SharedPreferences.Editor edit = mSharedPreferences.edit();
                                        edit.putString("serverurl",url);
                                        edit.apply();
                                        server_status = true;
                                    } else {
                                        Toast.makeText(requireContext(), "服务器不在线,请检查输入！", Toast.LENGTH_SHORT).show();
                                        server_status = false;
                                    }
                                });
                            }
                            @Override
                            public void onError(String errorMessage) {
                                requireActivity().runOnUiThread(() ->
                                        Log.e("NetworkCheck", "检测失败: " + errorMessage)
                                );
                                Toast.makeText(requireContext(), "服务器不在线,请检查输入！", Toast.LENGTH_SHORT).show();
                                server_status = false;
                            }
                        });
                    }else{
                        Toast.makeText(requireActivity(), "设置url失败，请检查输入！", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(requireContext(), "Url输入为空", Toast.LENGTH_SHORT).show();
                }
            }
        });



//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    /**
     * 检查权限方法
     */
    // 处理被拒绝的权限
    private void handleDeniedPermissions(List<String> deniedPermissions) {
        if (!shouldShowRequestPermissionRationale(deniedPermissions)) {
            showPermanentDeniedDialog(deniedPermissions);
        } else {
            showRationaleDialog(deniedPermissions);
        }
    }

    // 检查是否有权限需要显示解释说明
    private boolean shouldShowRequestPermissionRationale(List<String> permissions) {
        for (String perm : permissions) {
            if (shouldShowRequestPermissionRationale(perm)) {
                return true;
            }
        }
        return false;
    }

    // 显示权限说明对话框
    private void showRationaleDialog(List<String> deniedPermissions) {
        new AlertDialog.Builder(requireContext())
                .setTitle("需要权限说明")
                .setMessage("以下功能需要权限才能使用：\n" + getPermissionDescriptions(deniedPermissions))
                .setPositiveButton("重新授权", (dialog, which) -> retryPermissionRequest())
                .setNegativeButton("取消", null)
                .show();

    }

    // 显示永久拒绝后的对话框
    private void showPermanentDeniedDialog(List<String> deniedPermissions) {
        new AlertDialog.Builder(requireContext())
                .setTitle("部分权限被永久拒绝")
                .setMessage("请到应用设置中手动开启权限\n" + getPermissionDescriptions(deniedPermissions))
                .setPositiveButton("去设置", (dialog, which) -> openAppSettings())
                .setNegativeButton("取消", null)
                .show();
    }



    // 获取权限描述信息
    private String getPermissionDescriptions(List<String> permissions) {
        StringBuilder sb = new StringBuilder();
        int[] premission_flag = {0,0,0,0};

        for (String perm : permissions) {
            if (Arrays.asList(Permission.shot_PERMISSIONS).contains(perm) && premission_flag[0] == 0) {
                sb.append("• 相机权限\n");
                premission_flag[0] = 1;
            } else if (Arrays.asList(Permission.STORAGE_PERMISSIONS).contains(perm) && premission_flag[1] == 0) {
                sb.append("• 读写权限\n");
                premission_flag[1] = 1;
//            } else if (Arrays.asList(Permission.shot_PERMISSIONS).contains(perm)) {
//                sb.append("• 相机权限\n");
            } else if (Arrays.asList(Permission.location_PERMISSIONS).contains(perm) && premission_flag[2] == 0) {
                sb.append("• 定位权限\n");
                premission_flag[2] = 1;
            } else if (Arrays.asList(Permission.internet_PERMISSIONS).contains(perm) && premission_flag[3] == 0) {
                sb.append("• 网络权限\n");
                premission_flag[3] = 1;
            }

        }
        return sb.toString();
    }

    // 重新发起权限请求
    private void retryPermissionRequest() {
        permissionLauncher.launch(Permission.ALL_PERMISSIONS);
//        handleDeniedPermissions(Arrays.asList(ALL_PERMISSIONS));

    }

    // 打开应用设置
    @SuppressLint("QueryPermissionsNeeded")
    private void openAppSettings() {

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "无法打开设置页面", Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }


    /**
     * 图片获取方法
     */
    private void startCamera() {
        File photoFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg");
        imageUri = FileProvider.getUriForFile(requireContext(), "com.example.demo.fileprovider", photoFile);
        takePictureLauncher.launch(imageUri);
    }

    /**
     * 调用裁切库
     */
//    private void startCrop(Uri uri) {
//        Toast.makeText(requireContext(), "1", Toast.LENGTH_SHORT).show();
//        Uri destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), "cropped.jpg"));
//        Toast.makeText(requireContext(), "2", Toast.LENGTH_SHORT).show();
//        Intent intent = UCrop.of(uri, destinationUri)
//                .withAspectRatio(1, 1)
//                .withMaxResultSize(500, 500)
//                .getIntent(requireContext());
//        Toast.makeText(requireContext(), "3", Toast.LENGTH_SHORT).show();
//        cropImageLauncher.launch(intent);
//        Toast.makeText(requireContext(), "4", Toast.LENGTH_SHORT).show();
//    }
    private String generatedatetime() {
        // 创建日期格式化对象，指定日期的格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // 获取当前时间
        return sdf.format(new Date());
    }
    private String generateFileName() {
        // 创建日期格式化对象，指定日期的格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        // 获取当前时间
        String currentDateTime = sdf.format(new Date());
        // 生成的文件名可以是： "yyyyMMdd_HHmmss.txt" 或其他类型的文件扩展名
        return "camera_" + currentDateTime + ".jpg";  // 根据需要修改文件扩展名
    }
    private String changedatetime(String originaltime){
        String formattedDate = null;
        try {
            // 原始日期格式 "yyyy:MM:dd HH:mm:ss"
            @SuppressLint("SimpleDateFormat") SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            Date date = originalFormat.parse(originaltime);

            // 转换为目标格式 "yyyyMMdd_HHmmss"
            @SuppressLint("SimpleDateFormat") SimpleDateFormat targetFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            assert date != null;
            formattedDate = targetFormat.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return formattedDate;
    }
    private final int  colortype = -19903; //name="my_light_primary"---->#FFB241
    private void startCrop(Uri sourceUri) {
        Uri destinationUri = null;
        System.out.println("is_camera_falg: " + is_camera_falg);
        if(is_camera_falg){
            destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), generateFileName()));
            datetimetemp = generatedatetime();
        }else{
//            destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), getFileNameFromUri(sourceUri)));
//            getImageMetadata(sourceUri);
            getFileDetails(sourceUri);
            Map<String, String > fileInfo = getFileDetails(sourceUri);
            if(datetimetemp != null && changedatetime(datetimetemp) != null){
                destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), changedatetime(datetimetemp)+ ".jpg"));
                Log.d("1111","11111111111"+changedatetime(datetimetemp)+ ".jpg");
            }else if(fileInfo.get("dateAdded") != null){
                destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), changedatetime(fileInfo.get("dateAdded"))+ ".jpg") );
                Log.d("1111","11111111111"+fileInfo.get("dateAdded")+ ".jpg");
            }else{
                destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), fileInfo.get("fileName")));
                Log.d("1111","11111111111"+fileInfo.get("fileName"));
            }

        }


        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);
        options.setToolbarColor(colortype);
        options.setStatusBarColor(colortype);
        options.setFreeStyleCropEnabled(false);
        options.setShowCropGrid(true);

        Intent intent = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
//                .withMaxResultSize(224, 224)
                .withOptions(options)
                .getIntent(requireContext());

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        /**
         * 清零
         */
        pred_label = -1;
        pred_score = -1.0;
        pred_class = null;

        cropImageLauncher.launch(intent);


    }



    /**
     * 识别方法
     */


    // 上传裁切后的图片
    private void uploadImage (Uri croppedUri){


        // 获取文件路径（注意如果是content:// Uri，需要特别处理）
        String imagePath = getRealPathFromURI(croppedUri);

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Toast.makeText(requireActivity(), "File does not exist", Toast.LENGTH_SHORT).show();
            return ;
        }
        if(datetimetemp == null) {
            datetimetemp = "0000-00-00 00:00:00";
        }



        // 为其他表单字段创建RequestBody
        RequestBody timeBody = RequestBody.create(datetimetemp, MediaType.parse("text/plain"));
        RequestBody locationBody = RequestBody.create(Objects.requireNonNullElse(addresstemp, "NULL"), MediaType.parse("text/plain"));
        RequestBody latBody = RequestBody.create(String.valueOf(lattemp), MediaType.parse("text/plain"));
        RequestBody lngBody = RequestBody.create(String.valueOf(lngtemp), MediaType.parse("text/plain"));
        RequestBody reportidBody = RequestBody.create(Objects.requireNonNull(mSharedPreferences.getString("id", "null")),MediaType.parse("text/plain"));



        // 创建 RequestBody
        RequestBody requestBody = RequestBody.create(imageFile,MediaType.parse("image/*"));

        // 创建 MultipartBody.Part 来发送文件
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestBody);
        ApiService apiService = RetrofitClient.getClient(ServerConfig.serverurl).create(ApiService.class);

        // 创建上传请求
        Call<ResponseBody> call = apiService.uploadImage(timeBody, locationBody, latBody, lngBody, reportidBody, body);

        // 执行上传请求
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        // 获取响应体的字符串内容
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        // 解析返回的 JSON
                        pred_class = jsonResponse.getString("pred_class");
                        pred_label = jsonResponse.getInt("pred_label");
                        pred_score = jsonResponse.getDouble("pred_score");

                        // 显示返回的结果
//                        Toast.makeText(requireActivity(), "Pred Class:" + pred_class + ", Score:" + pred_score, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(requireContext(), ResultActivity.class);
                        intent.putExtra("image_uri", croppedUri); // toString()
                        intent.putExtra("result_label", pred_label);
                        intent.putExtra("result_class", pred_class);
                        intent.putExtra("result_score", pred_score);
                        intent.putExtra("result_address", addresstemp);
                        startActivity(intent);


                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireActivity(), "响应错误！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireActivity(), "上传失败！", Toast.LENGTH_SHORT).show();
                }
                datetimetemp = null;
                addresstemp = null;
                lattemp = 360;
                lngtemp = 360;
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireActivity(), "上传失败！", Toast.LENGTH_SHORT).show();
                datetimetemp = null;
                addresstemp = null;
                lattemp = 360;
                lngtemp = 360;
            }
        });
    }
    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        } else {
            return uri.getPath();
        }
    }
//    private String getFileNameFromUri(Uri uri) {
//        String fileName = null;
//        if (uri.getScheme().equals("content")) {
//            // 如果是content://，使用ContentResolver获取文件名
//            Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//                fileName = cursor.getString(nameIndex);
//                cursor.close();
//            }
//        } else if (uri.getScheme().equals("file")) {
//            // 如果是file://，直接从Uri中提取文件名
//            fileName = new File(uri.getPath()).getName();
//        }
//        return fileName;
//    }
    private void getImageMetadata(Uri imageUri) {
        try {
            // 使用ContentResolver获取图片的输入流
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
            ExifInterface exifInterface = new ExifInterface(inputStream);

            // 获取拍摄时间
            String dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            if (dateTime != null) {
                datetimetemp = dateTime;
                Log.d("ImageMetadata", "拍摄时间: " + dateTime);
            }else{
                datetimetemp = null;
            }
            // 获取纬度和经度
            double[] latLong = exifInterface.getLatLong();
            if (latLong != null) {
                lattemp = latLong[0];
                lngtemp = latLong[1];
                latlonToAddress(new LatLng(latLong[0], latLong[1]));
                Log.d("ImageMetadata", "照片纬度: " + latLong[0]);
                Log.d("ImageMetadata", "照片经度: " + latLong[1]);
//                System.out.println("照片纬度: " + latLong[0]);
//                System.out.println("照片经度: " + latLong[1]);
            } else {
                lattemp = 360;
                lngtemp = 360;
                Log.d("ImageMetadata", "照片纬度: NULL");
                Log.d("ImageMetadata", "照片经度: NULL");
//                System.out.println("照片纬度: NULL");
//                System.out.println("照片经度: NULL");
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ImageMetadata", "无法读取图片元数据");
            Toast.makeText(requireContext(), "无法读取图片元数据", Toast.LENGTH_SHORT).show();
        }
    }
    public Map<String, String> getFileDetails(Uri imageUri) {
        Map<String, String> fileDetails = new HashMap<>();
        String[] projection = {
                MediaStore.Images.Media.DISPLAY_NAME,  // 文件名
                MediaStore.Images.Media.SIZE,          // 文件大小
                MediaStore.Images.Media.DATE_ADDED     // 添加日期
        };

        ContentResolver contentResolver = requireActivity().getContentResolver();
        Cursor cursor = null;

        try {
            // 查询数据
            cursor = contentResolver.query(imageUri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {

                int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int dateAddedIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);

                Log.d("FileDetails", "nameIndex: " + nameIndex);
                Log.d("FileDetails", "sizeIndex: " + sizeIndex);
                Log.d("FileDetails", "dateAddedIndex: " + dateAddedIndex);

                if (nameIndex != -1 && sizeIndex != -1 && dateAddedIndex != -1) {
                    String fileName = cursor.getString(nameIndex);
                    String fileSize = String.valueOf(cursor.getLong(sizeIndex));
                    String dateAddedStr = String.valueOf(cursor.getLong(dateAddedIndex));

                    String formattedDate = null;
                    if (!"0".equals(dateAddedStr)) {
                        try {
                            // 将时间戳转换为日期
                            long dateAdded = Long.parseLong(dateAddedStr);
                            formattedDate = formatDate(dateAdded);
                        } catch (NumberFormatException e) {
                            Log.e("FileDetails", "Invalid date format: " + dateAddedStr);
                        }
                    }
                    Log.d("FileDetails", "fileName:"+fileName);
                    Log.d("FileDetails", "fileSize:"+fileSize);
                    Log.d("FileDetails", "dateAdded:"+(formattedDate != null ? formattedDate : null));
                    fileDetails.put("fileName", fileName);
                    fileDetails.put("fileSize", fileSize);
                    fileDetails.put("dateAdded", formattedDate != null ? formattedDate : null);
                } else {
                    Log.e("FileDetails", "Column index error!");
                }
            }
        } catch (Exception e) {
            Log.e("FileDetails", "Error occurred while retrieving file details", e);
        } finally {
            // 确保cursor最终被关闭
            if (cursor != null) {
                cursor.close();
            }
        }

        return fileDetails;
    }

    // 格式化日期
    private String formatDate(long timestamp) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timestamp * 1000);  // 时间戳是秒级的，需要转换为毫秒
        return sdf.format(date);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyLocation();
        binding = null;
    }

    public boolean isServer_status() {
        return server_status;
    }

    public void setServer_status(boolean server_status) {
        this.server_status = server_status;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (null != aMapLocation) {

            StringBuffer sb = new StringBuffer();
            //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
            if(aMapLocation.getErrorCode() == 0){
//                sb.append("定位成功" + "\n");
//                sb.append("定位类型: " + aMapLocation.getLocationType() + "\n");
//                sb.append("经    度    : " + aMapLocation.getLongitude() + "\n");
//                sb.append("纬    度    : " + aMapLocation.getLatitude() + "\n");
//                sb.append("精    度    : " + aMapLocation.getAccuracy() + "米" + "\n");
//                sb.append("提供者    : " + aMapLocation.getProvider() + "\n");
//
//                sb.append("速    度    : " + aMapLocation.getSpeed() + "米/秒" + "\n");
//                sb.append("角    度    : " + aMapLocation.getBearing() + "\n");
//                // 获取当前提供定位服务的卫星个数
//                sb.append("星    数    : " + aMapLocation.getSatellites() + "\n");
//                sb.append("国    家    : " + aMapLocation.getCountry() + "\n");
//                sb.append("省            : " + aMapLocation.getProvince() + "\n");
//                sb.append("市            : " + aMapLocation.getCity() + "\n");
//                sb.append("城市编码 : " + aMapLocation.getCityCode() + "\n");
//                sb.append("区            : " + aMapLocation.getDistrict() + "\n");
//                sb.append("区域 码   : " + aMapLocation.getAdCode() + "\n");
//                sb.append("地    址    : " + aMapLocation.getAddress() + "\n");
//                sb.append("兴趣点    : " + aMapLocation.getPoiName() + "\n");
                //定位完成的时间
//                sb.append("定位时间: " + Utils.formatUTC(aMapLocation.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
                lattemp = aMapLocation.getLatitude();
                lngtemp = aMapLocation.getLongitude();
                addresstemp = aMapLocation.getAddress();
//                Toast.makeText(requireActivity(), "定位地址："+aMapLocation.getAddress(), Toast.LENGTH_SHORT).show();
                if(addresstemp.length() == 0){
                    need_toast = true;
                    latlonToAddress(new LatLng(lattemp,lngtemp));
                }else{
                    Toast.makeText(requireActivity(), "定位地址："+addresstemp, Toast.LENGTH_SHORT).show();
                }
//                Toast.makeText(requireActivity(), "定位地址："+aMapLocation.getAddress(), Toast.LENGTH_SHORT).show();
//                Log.i("loaction","定位地址："+aMapLocation.getAddress() +aMapLocation.getAddress().length());
//                Toast.makeText(requireActivity(), "定位地址："+addresstemp, Toast.LENGTH_SHORT).show();
                Log.i("loaction","定位地址："+addresstemp+"     " +aMapLocation.getAddress().length());
                Log.i("loaction","定位地址："+aMapLocation.getAddress()+"     " +aMapLocation.getAddress().length());
            } else {
                //定位失败
//                sb.append("定位失败" + "\n");
//                sb.append("错误码:" + aMapLocation.getErrorCode() + "\n");
//                sb.append("错误信息:" + aMapLocation.getErrorInfo() + "\n");
//                sb.append("错误描述:" + aMapLocation.getLocationDetail() + "\n");
                lattemp = 360;
                lngtemp = 360;
                addresstemp = null;
                Toast.makeText(requireActivity(), "定位失败："+aMapLocation.getErrorCode(), Toast.LENGTH_SHORT).show();
                Log.w("loaction","定位失败："+aMapLocation.getErrorCode());
            }
//            sb.append("***定位质量报告***").append("\n");
//            sb.append("* WIFI开关：").append(aMapLocation.getLocationQualityReport().isWifiAble() ? "开启":"关闭").append("\n");
//            sb.append("* GPS状态：").append(getGPSStatusString(aMapLocation.getLocationQualityReport().getGPSStatus())).append("\n");
//            sb.append("* GPS星数：").append(aMapLocation.getLocationQualityReport().getGPSSatellites()).append("\n");
//            sb.append("* 网络类型：" + aMapLocation.getLocationQualityReport().getNetworkType()).append("\n");
//            sb.append("* 网络耗时：" + aMapLocation.getLocationQualityReport().getNetUseTime()).append("\n");
//            sb.append("****************").append("\n");
//            //定位之后的回调时间
//            sb.append("回调时间: " + Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

            //解析定位结果，
//            String result = sb.toString();
//            tvResult.setText(result);
        } else {
//            tvResult.setText("定位失败，loc is null");
            lattemp = 360;
            lngtemp = 360;
            addresstemp = null;
            Toast.makeText(requireActivity(), "定位失败：loc 为空！", Toast.LENGTH_SHORT).show();
            Log.w("loaction","定位失败：loc 为空！");
        }

        stopLocation();
    }
    /**
     * 初始化定位
     */
    private void initLocation() {
        //初始化定位
        try {
            mLocationClient = new AMapLocationClient(requireContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mLocationClient != null) {
            //设置定位回调监听
            mLocationClient.setLocationListener(this);
            //初始化AMapLocationClientOption对象
            mLocationOption = new AMapLocationClientOption();
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //获取最近3s内精度最高的一次定位结果：
            //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            mLocationOption.setOnceLocationLatest(true);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //设置定位请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
            mLocationOption.setHttpTimeOut(20000);
            //关闭缓存机制，高精度定位会产生缓存。
            mLocationOption.setLocationCacheEnable(false);
            //给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
        }
        try{
            geocodeSearch = new GeocodeSearch(requireContext());
        }catch (com.amap.api.services.core.AMapException e){
            e.printStackTrace();;
        }
        geocodeSearch.setOnGeocodeSearchListener(this);
    }
//    /**
//     * 坐标转地址
//     * @param regeocodeResult
//     * @param rCode
//     * */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        //解析result获取地址描述信息
        if(rCode == PARSE_SUCCESS_CODE){
            RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
            //显示解析后的地址
            if(regeocodeAddress.getFormatAddress() != null){
                addresstemp = regeocodeAddress.getFormatAddress();
                System.out.println("照片地址: " + addresstemp);
            }else{
                addresstemp = null;
                System.out.println("照片地址: NULL" );
            }
//            Toast.makeText(requireActivity(), "地址："+regeocodeAddress.getFormatAddress(), Toast.LENGTH_SHORT).show();
            if(need_toast){
                Toast.makeText(requireActivity(), "地址："+regeocodeAddress.getFormatAddress(), Toast.LENGTH_SHORT).show();
                need_toast = false;
            }
        }else {
//            Toast.makeText(requireActivity(), "获取地址失败", Toast.LENGTH_SHORT).show();
            addresstemp = null;
            System.out.println("照片地址: NULL" );
        }

    }

//    /**
//     * 地址转坐标
//     * @param geocodeResult
//     * @param rCode
//     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {

    }
//    /**
//     * 通过经纬度获取地址
//     * @param latLng
//     */
    private void latlonToAddress(LatLng latLng) {
        //位置点  通过经纬度进行构建
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        //逆编码查询  第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 20, GeocodeSearch.AMAP);
        //异步获取地址信息
        geocodeSearch.getFromLocationAsyn(query);

    }
//    /**
//     * 停止定位
//     */
    private void stopLocation(){
        try {
            // 停止定位
            mLocationClient.stopLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    /**
//     * 销毁定位
//     */
    private void destroyLocation(){
        if (null != mLocationClient) {
//            /**
//             * 如果AMapLocationClient是在当前Activity实例化的，
//             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
//             */
            mLocationClient.onDestroy();
            mLocationClient = null;
            mLocationOption = null;
        }
    }
}