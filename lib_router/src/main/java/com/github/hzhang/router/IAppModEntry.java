package com.github.hzhang.router;

import android.content.Context;

public interface IAppModEntry<T> {
    void onCreate();

    void attachBaseContext(Context base);

    default void onLowMemory() {}

    default void onTrimMemory(int level) {}

    IServiceProvider<T> createService();

    default void onAppHotExit() {}

    default void onAppHotStart() {}

    default void onDestroy() {}
}
