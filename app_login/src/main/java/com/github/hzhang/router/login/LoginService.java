package com.github.hzhang.router.login;

import androidx.annotation.NonNull;

import com.github.hzhang.router.login.api.IAppLoginService;

public class LoginService implements IAppLoginService {

    @NonNull
    public static LoginService getInstance() {
        return LoginServiceHolder.instance;
    }

    private static class LoginServiceHolder {
        private static final LoginService instance = new LoginService();
    }

    private LoginService() {}

    @Override
    public boolean isLogined() {
        return false;
    }

    @Override
    public String getCookie() {
        return "cookies";
    }
}
