package com.github.hzhang.router.biz.api;

import com.github.hzhang.router.anno.DefaultRet;
import com.github.hzhang.router.anno.ModuleSvs;

@ModuleSvs("AppBizModuleMgr")
public interface IAppBizService {
    String getBizId();

    void removeBizInfo();

    byte getByteValue();

    @DefaultRet("0")
    int getIntValue();

    Object getDefObject();
}
