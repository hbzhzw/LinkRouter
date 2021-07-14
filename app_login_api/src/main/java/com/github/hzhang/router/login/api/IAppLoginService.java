package com.github.hzhang.router.login.api;

import com.github.hzhang.router.anno.ModuleSvs;

@ModuleSvs
public interface IAppLoginService {
    boolean isLogined();
    default String getCookie() { return ""; }
}
