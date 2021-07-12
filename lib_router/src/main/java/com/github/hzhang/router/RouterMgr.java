package com.github.hzhang.router;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.github.hzhang.router.callback.IRouteCallback;
import com.github.hzhang.router.interceptor.IRouteProcessor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"FieldCanBeLocal", "unused", "RedundantSuppression"})
public class RouterMgr {
    private static final String TAG = "RouterMgr";
    private static Context mAppContext;
    private static final Map<String, Class<?>> mRouteInfo = new HashMap<>();
    private static IRouteProcessor mRouteProcessor;

    public static void dumpRouteInfo() {
        Log.d(TAG, "dump routeInfo: " + mRouteInfo);
    }

    @Keep
    public void initRoute(Context context, IRouteProcessor routeProcessor) {
        mAppContext = context;
        mRouteProcessor = routeProcessor;
    }

    public static void route(Context context, String routeId) {
        route(context, routeId, null, null);
    }

    public static void route(Context context, String routeId, Map<String, Object> params) {
        route(context, routeId, params, null);
    }

    @Keep
    public static void route(Context context,
            String routeId,
            Map<String, Object> params,
            IRouteCallback callback) {
        if (routeId != null && context != null) {
            boolean isHandled = handlePreRoute(context, routeId, params);
            if (!isHandled) {
                Class<?> clazz = mRouteInfo.get(routeId);
                if (clazz != null) {
                    isHandled = true;
                    Intent intent = new Intent(context, clazz);
                    encodeParams(intent, params);
                    context.startActivity(intent);
                } else {
                    handleRouteIdMissed(context, routeId, params);
                }
            }
            if (isHandled) {
                notifyCallbackFound(routeId, callback);
            } else {
                notifyCallbackLost(routeId, callback);
            }
        }
    }

    @Keep
    public static void addRoute(String routeId, Class<?> destAct) {
        if (routeId != null && destAct != null) {
            checkRoute(routeId, destAct);
            mRouteInfo.put(routeId, destAct);
        }
    }

    private static void checkRoute(@NonNull String routeId, @NonNull Class<?> destAct) {
        if (mRouteInfo.containsKey(routeId)) {
            throw new RuntimeException("routeId: " + routeId
                    + " already exist, value: " + destAct.getName());
        }
    }

    private static void notifyCallbackFound(String routeId, IRouteCallback callback) {
        if (callback != null) {
            callback.onFound(routeId);
        }
    }

    private static void notifyCallbackLost(String routeId, IRouteCallback callback) {
        if (callback != null) {
            callback.onLost(routeId);
        }
    }

    private static void encodeParams(@NonNull Intent intent, Map<String, Object> params) {
        if (params != null) {
            boolean isHandled = handleEncodeParams(intent, params);
            if (!isHandled) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        intent.putExtra(entry.getKey(), (String) value);
                    } else if (value instanceof Integer) {
                        intent.putExtra(entry.getKey(), (int) value);
                    } else if (value instanceof Long) {
                        intent.putExtra(entry.getKey(), (long) value);
                    } else if (value instanceof Bundle) {
                        intent.putExtra(entry.getKey(), (Bundle) value);
                    } else if (value instanceof Float) {
                        intent.putExtra(entry.getKey(), (float) value);
                    } else if (value instanceof Double) {
                        intent.putExtra(entry.getKey(), (double) value);
                    } else if (value instanceof Serializable) {
                        intent.putExtra(entry.getKey(), (Serializable) value);
                    }
                }
            }
            postHandleEncodeParams(intent, params);
         }
    }

    private static boolean handlePreRoute(Context context,
            String routeId,
            Map<String, Object> params) {
        return mRouteProcessor != null && mRouteProcessor.handlePreRoute(context, routeId, params);
    }

    private static boolean handleEncodeParams(@NonNull Intent intent, Map<String, Object> params) {
        return mRouteProcessor != null && mRouteProcessor.handleEncodeParams(intent, params);
    }

    private static void handleRouteIdMissed(Context context,
            String routeId,
            Map<String, Object> params) {
        if (mRouteProcessor != null) {
            mRouteProcessor.onRouteIdMissed(context, routeId, params);
        }
    }

    private static void postHandleEncodeParams(@NonNull Intent intent,
            Map<String, Object> params) {
        if (mRouteProcessor != null) {
            mRouteProcessor.postHandleEncodeParams(intent, params);
        }
    }
}
