package com.github.hzhang.router;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection",
                   "FieldCanBeLocal",
                   "UnusedReturnValue"})
public class ModulesMgr {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static final List<IAppModEntry<?>> mAppModules = new ArrayList<>();
    private static final Map<Class<?>, IServiceProvider<?>> mSvsProvMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> mSvsMap = new ConcurrentHashMap<>();

    public static void init(Context appContext) {
        mContext = appContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T service(@NonNull Class<T> clazz) {
        Object svs = mSvsMap.get(clazz);
        if (svs == null) {
            IServiceProvider<T> svsProv = (IServiceProvider<T>) mSvsProvMap.get(clazz);
            svs = svsProv != null ? svsProv.build() : null;
            if (svs != null) {
                mSvsMap.put(clazz, svs);
            }
        }
        return (T) svs;
    }

    public static <T> void register(Class<T> clazz,
            @NonNull IAppModEntry<T> appModule) {
        mAppModules.add(appModule);
        if (clazz != null) {
            registerSvsProv(clazz, appModule.createService());
        }
    }

    private static <T> boolean registerSvsProv(Class<T> clazz, IServiceProvider<T> servProv) {
        boolean isSuccess = false;
        if (mSvsProvMap.get(clazz) == null && servProv != null) {
            mSvsProvMap.put(clazz, servProv);
        }
        return isSuccess;
    }

    public static void notifyOnCreate() {
        for (IAppModEntry<?> modEntry : mAppModules) {
            modEntry.onCreate();
        }
    }

    public static void notifyAttachBaseContext(Context base) {
        for (IAppModEntry<?> modEntry : mAppModules) {
            modEntry.attachBaseContext(base);
        }
    }

    public static void notifyOnLowMemory() {
        for (IAppModEntry<?> modEntry : mAppModules) {
            modEntry.onLowMemory();
        }
    }

    public static void notifyOnTrimMemory(int level) {
        for (IAppModEntry<?> modEntry : mAppModules) {
            modEntry.onTrimMemory(level);
        }
    }

    public static void notifyHotStart() {
        for (IAppModEntry<?> modEntry : mAppModules) {
            modEntry.onAppHotStart();
        }
    }

    public static void notifyHotExit() {
        for (IAppModEntry<?> modEntry : mAppModules) {
            modEntry.onAppHotExit();
        }
    }

    public static void notifyDestroy() {
        for (IAppModEntry<?> modEntry : mAppModules) {
            modEntry.onDestroy();
        }
    }
}