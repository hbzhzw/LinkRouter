package com.github.hzhang.routerdemo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.github.hzhang.router.RouterMgr;
import com.github.hzhang.router.anno.AppSpec;

@AppSpec
public class AppLinkApplication extends Application {
    private static final String TAG = "AppLinkApplication";
    // private static final List<IAppModEntry<?>> mAppModules = new ArrayList<>();

    // private final List mAppModules = new ArrayList();

    // private void notifyOnCreate() {
    //     for (Object obj : mAppModules) {
    //         ((IAppModEntry) obj).onCreate();
    //     }
    // }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate appContext: " + getApplicationContext());
        super.onCreate();
        RouterMgr.dumpRouteInfo();
    }

    // @Override
    // public void onTerminate() {
    //     super.onTerminate();
    // }
    //
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(TAG, "attachBaseContext: " + base + ", appContext: " + getApplicationContext());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    //
    // @Override
    // public void onLowMemory() {
    //     super.onLowMemory();
    // }
    //
    // @Override
    // public void onTrimMemory(int level) {
    //     super.onTrimMemory(level);
    // }
}
