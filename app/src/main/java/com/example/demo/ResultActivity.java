package com.example.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result);


        //返回主界面
        findViewById(R.id.backtomain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //销毁返回
                finish();
            }
        });


        ImageView resultImage = findViewById(R.id.result_image);
        TextView result_label_Text = findViewById(R.id.result_label_text);
        TextView result_class_Text = findViewById(R.id.result_class_text);
        TextView result_score_Text = findViewById(R.id.result_score_text);
        TextView result_address_Text = findViewById(R.id.result_address_text);

        Intent intent = getIntent();
        Uri imageUri = intent.getParcelableExtra("image_uri");
        int result_label = intent.getIntExtra("result_label",-1);
        String result_class = intent.getStringExtra("result_class");
        double result_score = intent.getDoubleExtra("result_score",-1.0);
        String result_address = intent.getStringExtra("result_address");



        if (imageUri != null) {
            resultImage.setImageURI(imageUri);
        }


        // 打印日志验证数据
        Log.d("ResultActivity", "label: "+ result_label + "  class: "+ result_class + "  score: "+ result_score);

        result_label_Text.setText(result_label != -1 ? "推测标签："+result_label : "推测标签："+"无识别结果");
        result_class_Text.setText(result_class != null ? "推测种类："+result_class : "推测种类："+"无识别结果");
        result_score_Text.setText(result_score != -1.0 ? "推测分数："+result_score : "推测分数："+"无识别结果");
        result_address_Text.setText(result_address != null ? "地址："+result_address : "地址："+"NULL");

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
}