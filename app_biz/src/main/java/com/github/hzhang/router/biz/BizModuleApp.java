package com.github.hzhang.router.biz;

import android.content.Context;
import android.util.Log;

import com.github.hzhang.router.IAppModEntry;
import com.github.hzhang.router.IServiceProvider;
import com.github.hzhang.router.anno.ModuleSpec;
import com.github.hzhang.router.login.api.AppLoginApiMgr;

@SuppressWarnings({"unused", "RedundantSuppression"})
@ModuleSpec
public class BizModuleApp implements IAppModEntry<AppBizService> {
    private static final String TAG = "BizModuleApp";
    @Override
    public void onCreate() {
        String cookie = AppLoginApiMgr.getCookie();
        Log.d(TAG, "cookie from login app: " + cookie);
    }

    @Override
    public void attachBaseContext(Context base) {
        Log.d(TAG, "attachBaseContext");
    }

    @Override
    public IServiceProvider<AppBizService> createService() {
        return AppBizService::getInstance;
    }
}
