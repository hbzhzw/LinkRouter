package com.github.hzhang.router.login.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.hzhang.router.RouterMgr;
import com.github.hzhang.router.anno.RouteSpec;
import com.github.hzhang.router.login.R;

@RouteSpec(routeId = "51")
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.login_btn).setOnClickListener(
                v -> RouterMgr.route(LoginActivity.this, "1"));
    }
}