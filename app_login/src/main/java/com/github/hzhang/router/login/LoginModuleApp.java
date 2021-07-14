package com.github.hzhang.router.login;

import android.content.Context;
import android.util.Log;

import com.github.hzhang.router.IAppModEntry;
import com.github.hzhang.router.IServiceProvider;
import com.github.hzhang.router.anno.ModuleSpec;

@SuppressWarnings({"unused", "RedundantSuppression"})
@ModuleSpec
public class LoginModuleApp implements IAppModEntry<LoginService> {
    private Context mContext;
    private static final String TAG = "LoginModuleApp";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: " + mContext);
    }

    @Override
    public void attachBaseContext(Context base) {
        mContext = base;
        Log.d(TAG, "attachBaseContext, context: " + mContext);
    }

    @Override
    public IServiceProvider<LoginService> createService() {
        Log.d(TAG, "onCreateService");
        return LoginService::getInstance;
    }
}
