package com.github.hzhang.router.plugin.util


import javax.annotation.Nonnull

class ClassUtils {
    static boolean needHandle(String className) {
        return className != null &&
                className.length() > 0 &&
                !className.endsWith(".R") &&
                !className.startsWith("android") &&
                !className.startsWith("java") &&
                !className.startsWith("com.google") &&
                !className.startsWith("kotlin") &&
                !className.startsWith("META") &&
                !className.contains("R\$")
    }

//    static boolean isAppEntry(CtClass ctClass) {
//        return ctClass != null && ctClass.getAnnotation(AppSpec.class) != null
//    }

    //()Lcom/tencent/qqsports/router/IServiceProvider<Lcom/tencent/qqsports/router/biz/AppBizService;>;
    static String getSvsClassNameFromProvMethodSig(@Nonnull String svsProvSig) {
        String className = null;
        final String startMark = "<L"
        final String endMark = ";>;"
        int sIdx = svsProvSig.lastIndexOf(startMark)
        int eIdx = svsProvSig.lastIndexOf(endMark)
        if (sIdx >= 0 && eIdx >= 0) {
            className = svsProvSig.substring(sIdx + startMark.length(), eIdx);
            className = PathUtils.pathToClass(className)
        }
        return className;
    }

    static String camelName(String param) {
        String resultParam = null;
        if (param != null && param.length() > 0) {
            resultParam = param.substring(0, 1).toUpperCase() + param.substring(1)
        }
        return resultParam;
    }
}