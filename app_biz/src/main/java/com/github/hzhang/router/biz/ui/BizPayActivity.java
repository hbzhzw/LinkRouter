package com.github.hzhang.router.biz.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.hzhang.router.anno.RouteSpec;
import com.github.hzhang.router.biz.R;

@RouteSpec(routeId = "2")
public class BizPayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biz_pay);
    }
}