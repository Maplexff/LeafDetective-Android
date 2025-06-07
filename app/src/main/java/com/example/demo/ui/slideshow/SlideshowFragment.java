package com.example.demo.ui.slideshow;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.demo.HistoryAdapter;
import com.example.demo.HistoryItem;
import com.example.demo.NetworkUtils.ApiService;
import com.example.demo.NetworkUtils.HistoryItemResponse;
import com.example.demo.NetworkUtils.PaginatedResponse;
import com.example.demo.NetworkUtils.Pagination;
import com.example.demo.NetworkUtils.RetrofitClient;
import com.example.demo.NetworkUtils.ServerConfig;
import com.example.demo.R;
import com.example.demo.databinding.FragmentSlideshowBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SlideshowFragment extends Fragment implements AMapLocationListener,
        AMap.OnMapClickListener,
        LocationSource,
        GeocodeSearch.OnGeocodeSearchListener,
        AMap.OnMarkerClickListener{

    private FragmentSlideshowBinding binding;
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyList;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    private MapView mapView;
    //地图控制器
    public AMap aMap = null;
    private MyLocationStyle myLocationStyle = new MyLocationStyle();
    //位置更改监听
    private LocationSource.OnLocationChangedListener mListener;
    //地理编码搜索
    private GeocodeSearch geocodeSearch;
    private String addresstemp = null;
    //标点列表
// 创建存储Marker的集合
    private ArrayList<Marker> markerList = new ArrayList<>();
    private ArrayList<MarkerOptions> markerss = new ArrayList<>();
    //解析成功标识码
    private static final int PARSE_SUCCESS_CODE = 1000;
    private int currentPage = 1;  // 当前页面
    private boolean isLoading = false;  // 是否正在加载
    private static final int PAGE_SIZE = 10;  // 每页加载 10 条记录
    private PaginatedResponse paginatedResponse = null;
    private Pagination pagination = null;
    private int clicktemp = -1;
    private int totalitem = 0;
    private boolean is_full = false;
    private BitmapDescriptor marker_blue = null;
    private BitmapDescriptor marker_red = null;
    private LatLng latLngtemp = null;
    private boolean isFragmentActive = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        mapView = root.findViewById(R.id.amap_view);
        mapView.onCreate(savedInstanceState);
        initLocation();
        initMap(savedInstanceState);
        latLngtemp = new LatLng(90.0,0.0);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        // 初始化数据
        historyList = new ArrayList<>();
        markerList = new ArrayList<>();
        markerss = new ArrayList<>();
        marker_blue = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        marker_red = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);



        // 设置适配器
        historyAdapter = new HistoryAdapter(historyList, item -> {
            if(isLoading){
                Toast.makeText(requireActivity(), "Marker还未获取！", Toast.LENGTH_SHORT).show();
                return;
            }
            // 处理点击事件
//            Toast.makeText(requireActivity(), "点击了：" + item.getItemId(), Toast.LENGTH_SHORT).show();
//            Toast.makeText(requireActivity(), "clicktemp：" + clicktemp, Toast.LENGTH_SHORT).show();
            if(clicktemp > -1){

                Log.i("Marker","Marker:"+(clicktemp+1)+"改蓝色");
                changemaerkericon(clicktemp,false);

            }

            if(item.getItemLatLng() != null && !item.getItemLatLng().equals(latLngtemp)){
                clicktemp = item.getItemId() - 1;

                Log.i("Marker","Marker:"+(clicktemp+1)+"改红色");
                updateMapCenter(item.getItemLatLng());

                changemaerkericon(clicktemp,true);

            }else{
                Toast.makeText(requireActivity(), "无地址或坐标非法！", Toast.LENGTH_SHORT).show();
            }

        });
        recyclerView.setAdapter(historyAdapter);
        // 加载初始数据
        fetchRecords(currentPage, PAGE_SIZE);
        // 设置下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 下拉刷新时重新加载第一页
            if(clicktemp > -1){
                changemaerkericon(clicktemp,false);
            }

//            clearAllMarker();
            aMap.clear(true);
            historyAdapter.clearData();
            currentPage = 1;
            historyList.clear();  // 如果是第一页，清空数据
            markerss.clear();
            markerList.clear();
            clicktemp = -1;
            totalitem = 0;
            is_full = false;
            pagination = null;
            paginatedResponse = null;

            fetchRecords(currentPage, PAGE_SIZE);
        });

        // 添加滚动监听器，检测是否滑动到底部
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && !isLoading){
                    if (!recyclerView.canScrollVertically(1)) {
                        if(clicktemp > -1){
                            changemaerkericon(clicktemp,false);
                        }
                        clicktemp = -1;
                        // 滑动到底部
                        if(pagination.isHasNextPage()){
                            currentPage++;
                        }

                        fetchRecords(currentPage, PAGE_SIZE);


//                        if (isLoading) {
//                            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()) {
//                                @Override
//                                public boolean canScrollVertically() {
//                                    return false;  // 禁用滚动
//                                }
//                            });
//                        }else {
//                            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()) {
//                                @Override
//                                public boolean canScrollVertically() {
//                                    return true;  // 禁用滚动
//                                }
//                            });
//                        }
                    }
                }

            }
        });

//        final TextView textView = binding.textSlideshow;
//        slideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    // 数据解析
    @SuppressLint("NotifyDataSetChanged")
    private void loadData(int page, int pageSize) {
        isLoading = true;
        historyAdapter.isloadingflag = true;
//        swipeRefreshLayout.setRefreshing(true);  // 显示刷新动画
        if (isAdded()) {
            swipeRefreshLayout.setRefreshing(true);  // 显示刷新动画
        }
        // 数据解析
        new Handler().postDelayed(() -> {
            if (!isAdded() || getContext() == null) {
                return;
            }
            if (pagination.getCurrentRecord() > 0) {
                List<HistoryItemResponse> data = paginatedResponse.getData();
                int i = 1;
                for (HistoryItemResponse item : data) {
//                    historyList.add(new HistoryItem((page-1) * pageSize + i,item.getTime(),item.getLocation(),String.valueOf(item.getReportid()),item.getImageid(),item.getImageBase64(), item.getPredclass(), item.getPredscore(), new LatLng(item.getLat(),item.getLng())));  // 浅拷贝（直接引用原对象）
                    if(-90 <=item.getLat() && item.getLat() <= 90 && -180 <=item.getLng() && item.getLng() <= 180){
                        if(item.getLocation().length() == 0){
                            latlonToAddress(new LatLng(item.getLat(),item.getLng()));
                            historyList.add(new HistoryItem((page-1) * pageSize + i,item.getTime(),addresstemp,String.valueOf(item.getReportid()),item.getImageid(),item.getImageBase64(), item.getPredclass(), item.getPredscore(), new LatLng(item.getLat(),item.getLng())));  // 浅拷贝（直接引用原对象）
                        }else{
                            historyList.add(new HistoryItem((page-1) * pageSize + i,item.getTime(),item.getLocation(),String.valueOf(item.getReportid()),item.getImageid(),item.getImageBase64(), item.getPredclass(), item.getPredscore(), new LatLng(item.getLat(),item.getLng())));  // 浅拷贝（直接引用原对象）
                        }
//                        historyList.add(new HistoryItem((page-1) * pageSize + i,item.getTime(),item.getLocation(),String.valueOf(item.getReportid()),item.getImageid(),item.getImageBase64(), item.getPredclass(), item.getPredscore(), new LatLng(item.getLat(),item.getLng())));  // 浅拷贝（直接引用原对象）
                        MarkerOptions options = new MarkerOptions()
                                .position(new LatLng(item.getLat(), item.getLng()))
                                .title("MARKER" + ((page-1) * pageSize + i))
                                .icon(marker_blue);
                        Log.d("Marker","MARKER" + ((page-1) * pageSize + i));
                        Log.d("Marker","Lat:" + item.getLat()+" Lng:"+item.getLng());
                        markerss.add(options);
                        // 关键：保存addMarker返回的Marker对象"

                        Marker marker = aMap.addMarker(options);
                        markerList.add(marker);
                    }else{
                        historyList.add(new HistoryItem((page-1) * pageSize + i,item.getTime(),item.getLocation(),String.valueOf(item.getReportid()),item.getImageid(),item.getImageBase64(), item.getPredclass(), item.getPredscore(), new LatLng(90.0,0.0)));  // 浅拷贝（直接引用原对象）
                        MarkerOptions options = new MarkerOptions()
                                .position(new LatLng(90.0, 0.0))
                                .title("MARKER" + ((page - 1) * pageSize + i))
                                .icon(marker_blue)
                                .visible(false);
                        Log.d("Marker","非法MARKER" + ((page-1) * pageSize + i));
                        Log.d("Marker","Lat:" + item.getLat()+" Lng:"+item.getLng());
                        markerss.add(options);
                        // 关键：保存addMarker返回的Marker对象"
                        Marker marker = aMap.addMarker(options);
                        marker.setClickable(false);
                        markerList.add(marker);
                    }
                    i++;
                }

                historyAdapter.notifyDataSetChanged();

            } else {
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(getActivity(), "没有更多数据了", Toast.LENGTH_SHORT).show();
                }
            }

            isLoading = false;
            historyAdapter.isloadingflag = false;
            swipeRefreshLayout.setRefreshing(false);
        }, 2000);
    }


    private void fetchRecords(int page, int pageSize) {
        isLoading = true;
        historyAdapter.isloadingflag = true;
        swipeRefreshLayout.setRefreshing(true);  // 显示刷新动画
        ApiService apiService = RetrofitClient.getClient(ServerConfig.serverurl).create(ApiService.class);
        apiService.getRecords(page, pageSize).enqueue(new Callback<PaginatedResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse> call, @NonNull Response<PaginatedResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    paginatedResponse = response.body();

                    if (paginatedResponse.getPagination() == null) {
                        // 如果成功标志为 false，直接返回并显示错误消息
                        if (isAdded() && getActivity()!=null){
                            Toast.makeText(getActivity(), "服务器请求失败！", Toast.LENGTH_SHORT).show();
                        }

                        Log.e("record", "Error: paginatedResponsenull");
                        isLoading = false;
                        historyAdapter.isloadingflag = false;
                        swipeRefreshLayout.setRefreshing(false);
                        currentPage--;
                        return;
                    }

                    // 获取分页数据
                    pagination = paginatedResponse.getPagination();


                    Log.d("record", "Current Page: " + pagination.getCurrentPage());
                    Log.d("record", "Current Record: " + pagination.getCurrentRecord());
                    Log.d("record", "Total Pages: " + pagination.getTotalPages());
                    Log.d("record", "Total Records: " + pagination.getTotalRecords());
                    Log.d("record", "Records: " + paginatedResponse.getData());
                    if(totalitem < pagination.getTotalRecords()){
                        totalitem = totalitem + pagination.getCurrentRecord();
                    }
                    Log.d("record", "totalitem: " + totalitem);
                    if(totalitem < pagination.getTotalRecords()){
                        is_full = false;
                    }
                    if(is_full){

                        if (isAdded() && getActivity()!=null){
                            Toast.makeText(getActivity(), "没有更多数据了", Toast.LENGTH_SHORT).show();
                        }

                        isLoading = false;
                        historyAdapter.isloadingflag = false;
                        swipeRefreshLayout.setRefreshing(false);
                        return;
                    }
                    loadData(page,pageSize);
                    showpointlocation(markerss);
                    if(totalitem == pagination.getTotalRecords()){
                        is_full = true;
                    }
                } else {
                    if (isAdded() && getActivity()!=null) {
                        Toast.makeText(getActivity(), "服务器请求失败！", Toast.LENGTH_SHORT).show();
                    }


                    Log.e("record", "Error: " + response.message());
                    isLoading = false;
                    historyAdapter.isloadingflag = false;
                    swipeRefreshLayout.setRefreshing(false);

                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedResponse> call, @NonNull Throwable t) {
                if (isAdded() && getActivity()!=null) {
                    Toast.makeText(getActivity(), "服务器请求失败！", Toast.LENGTH_SHORT).show();
                }
                Log.e("record", "Request failed", t);
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }
//    /**
//     * 初始化地图
//     * @param savedInstanceState
//     */
    private void initMap(Bundle savedInstanceState) {
        //初始化地图控制器对象
        aMap = mapView.getMap();

        // 自定义定位蓝点图标
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointlocation));
        // 自定义精度范围的圆形边框颜色  都为0则透明
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        // 自定义精度范围的圆形边框宽度  0 无宽度
        myLocationStyle.strokeWidth(0);
        // 设置圆形的填充颜色  都为0则透明
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));

        //设置定位蓝点的Style
        aMap.setMyLocationStyle(myLocationStyle);
        UiSettings mUiSettings = aMap.getUiSettings();
        mUiSettings.setCompassEnabled(true);//指南针
        mUiSettings.setScaleControlsEnabled(true);//比例尺
        mUiSettings.setZoomControlsEnabled(true);//缩放控件

        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        aMap.setOnMarkerClickListener(this);

        try{
            geocodeSearch = new GeocodeSearch(requireContext());
        }catch (com.amap.api.services.core.AMapException e){
            e.printStackTrace();;
        }
        geocodeSearch.setOnGeocodeSearchListener(this);
    }

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
//                Toast.makeText(requireActivity(), "定位地址："+aMapLocation.getAddress(), Toast.LENGTH_SHORT).show();
                Log.i("loaction","定位地址："+aMapLocation.getAddress());
            } else {
//                //定位失败
//                sb.append("定位失败" + "\n");
//                sb.append("错误码:" + aMapLocation.getErrorCode() + "\n");
//                sb.append("错误信息:" + aMapLocation.getErrorInfo() + "\n");
//                sb.append("错误描述:" + aMapLocation.getLocationDetail() + "\n");
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
            Toast.makeText(requireActivity(), "定位失败：loc 为空！", Toast.LENGTH_SHORT).show();
            Log.w("loaction","定位失败：loc 为空！");
        }
        stopLocation();

        if(mListener != null){
            mListener.onLocationChanged(aMapLocation);
        }
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
    }
//    /**
//     * 坐标转地址
//     * @param regeocodeResult
//     * @param rCode
//     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        //解析result获取地址描述信息
        if(rCode == PARSE_SUCCESS_CODE){
            RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
            //显示解析后的地址
            Toast.makeText(requireActivity(), "地址："+regeocodeAddress.getFormatAddress(), Toast.LENGTH_SHORT).show();
            addresstemp = regeocodeAddress.getFormatAddress();
        }else {
            addresstemp = "NULL";
            Toast.makeText(requireActivity(), "获取地址失败", Toast.LENGTH_SHORT).show();
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
//     * 地图单击事件
//     * @param latLng
//     */
    @Override
    public void onMapClick(LatLng latLng) {
        Toast.makeText(requireActivity(), "点击了地图，经度："+latLng.longitude+"，纬度："+latLng.latitude, Toast.LENGTH_SHORT).show();
    }

    public static int extractMarkerNumber(String input) {
        if (input == null || input.isEmpty()) return -1;

        try {
            // 方法1：正则表达式直接提取数字部分
//            String numberStr = input.replaceAll("[^0-9]", ""); // 移除非数字字符
//            return Integer.parseInt(numberStr);

            // 方法2：正则分组匹配（更严格校验格式）

            Pattern pattern = Pattern.compile("MARKER(\\d+)");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
            }

        } catch (NumberFormatException e) {
            Log.e("MarkerParser", "数字转换失败: " + e.getMessage());
            return -1;
        }
        return -1;
    }
//    /**
//     * Marker点击事件
//     * @param marker
//     * @return
//     */
    @Override
    public boolean onMarkerClick(Marker marker) {
//        showMsg("点击了标点");

//        int number = extractMarkerNumber(marker.getId()); // 输出：123
        int number = extractMarkerNumber(marker.getTitle()); // 输出：123
//        Toast.makeText(requireActivity(), "marker---"+ marker.getTitle(), Toast.LENGTH_SHORT).show();
        Toast.makeText(requireActivity(), "标号："+ number, Toast.LENGTH_SHORT).show();
        number = number - 1;
        if (number > -1) {
            try {
                if(clicktemp > -1){
                    Log.i("Marker","Marker:"+(clicktemp+1)+"改蓝色");

                    changemaerkericon(clicktemp,false);
                }
                clicktemp = number ;
                Log.i("Marker","Marker:"+(clicktemp+1)+"改红色");

                updateMapCenter(marker.getPosition());

                changemaerkericon(clicktemp,true);
                if (clicktemp >= 0 && clicktemp < historyAdapter.getItemCount()) {
                    // 滚动到指定位置
                    recyclerView.scrollToPosition(clicktemp);
                    // 设置选中项
                    historyAdapter.setSelectedPosition(clicktemp);
                } else {
                    Toast.makeText(requireActivity(), "滑动id："+ clicktemp, Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireActivity(), "无效的项编号点击", Toast.LENGTH_SHORT).show();
            }
        }
        return true;

    }

    private void showpointlocation(ArrayList<MarkerOptions> markeroptionslist){

        //添加标点
        aMap.addMarkers(markeroptionslist, false);
        Log.i("Maker","markeroptionslist_size  "+ markerList.size());
//        Marker marker = aMap.addMarker(new MarkerOptions().position(latLng).snippet("DefaultMarker"));
//        markerList.add(marker);
//        aMap.addMarkers();
    }

    private void changemaerkericon(int indexToModify,boolean rgbflag){
        // 假设是根据索引 n 找到要修改的 HistoryItem 和 Marker
//        int indexToModify = n;
//        HistoryItem oldItem = historyList.get(indexToModify);
        Marker oldMarker = markerList.get(indexToModify);
// 创建新的 HistoryItem 和 Marker
//        HistoryItem newItem = new HistoryItem(newId, newTime, newLocation, newReportId, newImageId, newImageBase64, newPredClass, newPredScore, new LatLng(newLat, newLng));
        if(rgbflag){
            MarkerOptions newOptions = new MarkerOptions()
                    .position(markerList.get(indexToModify).getPosition())
                    .title(markerList.get(indexToModify).getTitle())
                    .icon(marker_red);
            Marker newMarker = aMap.addMarker(newOptions);
            markerss.set(indexToModify, newOptions);
            markerList.set(indexToModify, newMarker);
            aMap.addMarker(newOptions);
            oldMarker.destroy();
        }else {
            MarkerOptions newOptions = new MarkerOptions()
                    .position(markerList.get(indexToModify).getPosition())
                    .title(markerList.get(indexToModify).getTitle())
                    .icon(marker_blue);
            Marker newMarker = aMap.addMarker(newOptions);
            markerss.set(indexToModify, newOptions);
            markerList.set(indexToModify, newMarker);
            aMap.addMarker(newOptions);
            oldMarker.destroy();
        }

    }



//删除指定Marker
    private void clearAllMarker() {
        //获取地图上所有Marker
        List<Marker> mapScreenMarkers = aMap.getMapScreenMarkers();
        for (int i = 0; i < markerList.size(); i++) {
            Marker marker = markerList.get(i);
            marker.destroy();//移除当前Marker
            markerList.remove(i);
            Log.i("Maker","clear"+i);
        }
        markerss.clear();
        markerList.clear();
    }
    /**
     * 改变地图中心位置
     * @param latLng 位置
     */
    public void updateMapCenter(LatLng latLng) {
        // CameraPosition 第一个参数： 目标位置的屏幕中心点经纬度坐标。
        // CameraPosition 第二个参数： 目标可视区域的缩放级别
        // CameraPosition 第三个参数： 目标可视区域的倾斜度，以角度为单位。
        // CameraPosition 第四个参数： 可视区域指向的方向，以角度为单位，从正北向顺时针方向计算，从0度到360度
        CameraPosition cameraPosition = new CameraPosition(latLng, 12, 30, 0);
        //位置变更
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        //改变位置
//        aMap.moveCamera(cameraUpdate);
        aMap.animateCamera(cameraUpdate);
    }
    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient != null) {
            mLocationClient.startLocation();//启动定位
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
        aMap.removecache();
    }
    /**
     * 停止定位
     */
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

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();

    }
    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        aMap.removecache();
        destroyLocation();
        isFragmentActive = false;
        binding = null;
    }
}