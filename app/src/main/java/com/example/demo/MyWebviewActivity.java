package com.example.demo;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.example.demo.R;
import com.just.agentweb.AgentWeb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.widget.PopupMenu;

public class MyWebviewActivity extends AppCompatActivity {

    private AgentWeb agentWeb;
    private Spinner spinner;
    private EditText searchcontent;

    private Map<String, String> searchEngines = new HashMap<>();
    private String baseUrl;



    public static void start(Context context, String keyword) {
        Intent intent = new Intent(context, MyWebviewActivity.class);
        intent.putExtra("keyword", keyword);
        context.startActivity(intent);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mywebview);
//        searchcontent = findViewById(R.id.searchcontentwebview);
        String keyword = getIntent().getStringExtra("keyword");
//        loadEngines();
//        setupSpinner();

        baseUrl = "https://www.bing.com/search?q=";

        agentWeb = AgentWeb.with(this)
                .setAgentWebParent(findViewById(R.id.container), new android.widget.FrameLayout.LayoutParams(-1, -1))
//                .setCustomIndicator(findViewById(R.id.progressBar))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go(baseUrl + keyword);


//        findViewById(R.id.searcherbuttonwebview).setOnClickListener(v -> {
//            String key = searchcontent.getText().toString().trim();
//            agentWeb.getUrlLoader().loadUrl(baseUrl + key);
//        });

        findViewById(R.id.backtomain).setOnClickListener(v -> {
            finish();
        });

        findViewById(R.id.pageback).setOnClickListener(v -> {
            if (agentWeb.getWebCreator().getWebView().canGoBack()) {
                agentWeb.getWebCreator().getWebView().goBack();
            } else {
                // 无历史记录时关闭当前页面
                //finish();
                Toast.makeText(MyWebviewActivity.this, "已无法回退！", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.pageforward).setOnClickListener(v -> {
            if (agentWeb.getWebCreator().getWebView().canGoForward()) {
                agentWeb.getWebCreator().getWebView().goForward();
            }else {
                // 无历史记录时关闭当前页面
                //finish();
                Toast.makeText(MyWebviewActivity.this, "已无法前进！", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.pagerefresh).setOnClickListener(v -> {
            //finish();
            agentWeb.getWebCreator().getWebView().reload();
        });

        findViewById(R.id.menuwebview).setOnClickListener(v -> {
            //finish();

            showPopupMenu(v,keyword);


        });


//        findViewById(R.id.btnBack).setOnClickListener(v -> agentWeb.back());
//        findViewById(R.id.btnForward).setOnClickListener(v -> agentWeb.getWebCreator().getWebView().goForward());
//        findViewById(R.id.btnRefresh).setOnClickListener(v -> agentWeb.getWebCreator().getWebView().reload());

    }

    private void showPopupMenu(View anchorView ,String keyword) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.mywebview_menu, popupMenu.getMenu());

//        // 动态添加菜单项示例
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            popupMenu.getMenu().add("夜间模式").setIcon(R.drawable.ic_night_mode);
//        }

        // 菜单项点击处理
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_baidu) {
                Toast.makeText(MyWebviewActivity.this, "当前引擎：Baidu", Toast.LENGTH_SHORT).show();
                baseUrl = "https://www.baidu.com/s?wd=";
                agentWeb.getUrlLoader().loadUrl(baseUrl + keyword);
                return true;
            } else if (id == R.id.menu_360) {
                Toast.makeText(MyWebviewActivity.this, "当前引擎：360", Toast.LENGTH_SHORT).show();
                baseUrl = ("https://www.so.com/s?ie=utf-8&shb=1&src=noscript_home&q=");
                agentWeb.getUrlLoader().loadUrl(baseUrl + keyword);
                return true;
            } else if (id == R.id.menu_bing) {
                Toast.makeText(MyWebviewActivity.this, "当前引擎：Bing", Toast.LENGTH_SHORT).show();
                baseUrl = "https://www.bing.com/search?q=";
                agentWeb.getUrlLoader().loadUrl(baseUrl + keyword);
                return true;
            } else if (id == R.id.menu_google) {
                Toast.makeText(MyWebviewActivity.this, "当前引擎：Google", Toast.LENGTH_SHORT).show();
                baseUrl = " https://www.google.com/search?q=";
                agentWeb.getUrlLoader().loadUrl(baseUrl + keyword);
                return true;
            }
            return false;
        });
        // 显示弹窗
        popupMenu.show();
    }




//        private void loadEngines() {
//        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
//        String json = sp.getString("search_engines", "{}");
//        searchEngines = new Gson().fromJson(json, HashMap.class);
//        if (searchEngines.isEmpty()) {
//            searchEngines.put("Baidu", "https://www.baidu.com/s?wd=");
//            searchEngines.put("Bing", "https://www.bing.com/search?q=");
//            searchEngines.put("Google", "https://www.google.com/search?q=");
//        }
//
//        baseUrl = searchEngines.values().iterator().next();
//
//        baseUrl = "https://www.bing.com/search?q=";
//
//    }
//
//    private void setupSpinner() {
//        ArrayList<String> keys = new ArrayList<>(searchEngines.keySet());
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, keys);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//
//        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
//                String selected = keys.get(position);
//                baseUrl = searchEngines.get(selected);
//            }
//
//            @Override
//            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
//        });
//    }






//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

    @Override
    protected void onResume() {
        agentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        agentWeb.getWebLifeCycle().onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        agentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }
}


