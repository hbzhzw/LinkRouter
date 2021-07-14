package com.github.hzhang.router.biz.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.hzhang.router.anno.RouteSpec;
import com.github.hzhang.router.biz.R;

@RouteSpec(routeId = "1")
public class BizMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biz_main);
    }
}