package com.github.hzhang.router.callback;

public interface IRouteCallback {
    void onFound(String routeId);
    void onLost(String routeId);
}
