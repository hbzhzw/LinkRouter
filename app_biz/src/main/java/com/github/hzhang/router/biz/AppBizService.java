package com.github.hzhang.router.biz;

import androidx.annotation.NonNull;

import com.github.hzhang.router.biz.api.IAppBizService;

public class AppBizService implements IAppBizService {

    @NonNull
    public static AppBizService getInstance() {
        return AppBizServiceHolder.instance;
    }

    private static class AppBizServiceHolder {
        private static final AppBizService instance = new AppBizService();
    }

    private AppBizService() {
    }

    @Override
    public String getBizId() {
        return "bizId";
    }

    @Override
    public void removeBizInfo() {

    }

    @Override
    public byte getByteValue() {
        return 0;
    }

    @Override
    public int getIntValue() {
        return 0;
    }

    @Override
    public Object getDefObject() {
        return null;
    }
}
