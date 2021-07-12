package com.github.hzhang.router.interceptor;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.Map;

public interface IRouteProcessor {
    boolean handlePreRoute(Context context, String routeId, Map<String, Object> params);

    boolean handleEncodeParams(@NonNull Intent intent, Map<String, Object> params);

    boolean onRouteIdMissed(Context context, String routeId, Map<String, Object> params);

    void postHandleEncodeParams(@NonNull Intent intent, Map<String, Object> params);
}
