package com.github.hzhang.router.plugin.bcode

import com.github.hzhang.router.anno.AppSpec
import com.github.hzhang.router.anno.ModuleSpec
import com.github.hzhang.router.anno.ModuleSvs
import com.github.hzhang.router.anno.RouteSpec
import com.github.hzhang.router.plugin.jarfile.JarFileHandler
import com.github.hzhang.router.plugin.log.Loger
import com.github.hzhang.router.plugin.util.ClassUtils
import com.github.hzhang.router.plugin.util.PathUtils
import groovy.io.FileType
import javassist.*
import org.apache.commons.io.FileUtils

import javax.annotation.Nonnull
import java.util.jar.JarFile

class CodeGenerateMgr {
    private static final String TAG = "RouteTransform_CodeGenerateMgr"
    private static final CREATE_METHOD = "onCreate"
    private static final LOW_MEM_METHOD = "onLowMemory"
    private static final TRIM_MEM_METHOD = "onTrimMemory"
    private static final ATTACH_BASE_CONTENT_METHOD = "attachBaseContext"

    private static final INIT_APP_MODULE_METHOD = "initAppModules"
    private static final MODULE_APP_CRETAE_SVS_PROV = "createService"
    private static final String MODULES_MGR = "com.github.hzhang.router.ModulesMgr"
    private static final String ROUTER_MGR = "com.github.hzhang.router.RouterMgr"

    static CtClass findAppEntryCls(@Nonnull File dest) {
        CtClass appEntryCls = null
        ClassPool classPool = RouteClassPool.getClassPool()
        int clsNameStartIdx = dest.path.length() + File.separator.length()
        dest.eachFileRecurse(FileType.FILES) {
            String className = PathUtils.classNameFromPath(it.path, clsNameStartIdx)
            Loger.d(TAG, "entry: ${it.path}, className: ${className}")
            if (appEntryCls == null && ClassUtils.needHandle(className)) {
                CtClass ctClass = classPool.get(className)
                if (ctClass.getAnnotation(AppSpec.class) != null) {
                    Loger.i(TAG, "app entry cls: ${ctClass.name}" +
                            ", destDir: ${dest.path}")
                    appEntryCls = ctClass
                } else {
                    ctClass.detach()
                }
            }
        }
        return appEntryCls
    }

    static CtClass transformModuleJar(File jarSrcFile, File dest) {
        CtClass moduleAppCls
        Map<RouteSpec, CtClass> routeClsMap = new HashMap<>()
        ClassPool classPool = RouteClassPool.getClassPool()
        JarFile jarFile = new JarFile(jarSrcFile)
        jarFile.stream().forEach() {
            String className = PathUtils.classNameFromPath(it.name)
            if (ClassUtils.needHandle(className)) {
                boolean isDetach = false
                CtClass ctClass = classPool.get(className)
                Object anno = ctClass.getAnnotation(ModuleSpec.class)
                if (anno != null) {
                    if (moduleAppCls == null) {
                        moduleAppCls = ctClass
                        isDetach = false
                    } else {
                        Loger.e(TAG, "module entry which annotated with ModuleSvs" +
                                " should only has one, but now has two: " +
                                "${moduleAppCls.name} and ${ctClass.name}")
                    }
                }
                anno = ctClass.getAnnotation(RouteSpec.class)
                if (anno != null) {
                    routeClsMap.put((RouteSpec) anno, ctClass)
                    isDetach = false
                }
                if (isDetach) {
                    ctClass.detach()
                }
            }
        }
        if (moduleAppCls != null && routeClsMap != null && routeClsMap.size() > 0) {
            Loger.i(TAG, "moduleAppCls: ${moduleAppCls.name}, dest: ${dest.path}")
            insertModuleRouteInfo(moduleAppCls, routeClsMap)
            JarFileHandler.writeJarFile(jarFile, dest, moduleAppCls)
        } else {
            FileUtils.copyFile(jarSrcFile, dest)
        }
        return moduleAppCls
    }

    static void insertModuleRouteInfo(@Nonnull CtClass moduleAppCls,
                                      @Nonnull Map<RouteSpec, CtClass> routeClsMap) {
        CtMethod onCreateMethod = moduleAppCls.getDeclaredMethod(CREATE_METHOD)
        StringBuilder routeTblBuilder = new StringBuilder()
        routeClsMap.forEach() { key, val ->
            String routeId = key.routeId()
            if (routeId == null || routeId.length() <=0) {
                Loger.e(TAG, "${val.name} should not have empty routeId!")
            } else {
                routeTblBuilder.append("${ROUTER_MGR}.addRoute(\n")
                        .append("    \"${routeId}\", ${val.name}.class);\n")
            }
        }
        String routeTblCode = routeTblBuilder.toString()
        Loger.d(TAG, "route tbl code: \n${routeTblCode}")
        onCreateMethod.insertBefore(routeTblCode)
    }

    static void generateEntryCode(Map<CtClass, String> appClsEntryMap,
                                  List<CtClass> moduleEntryClsList) {
        for (Map.Entry<CtClass, String> entry : appClsEntryMap.entrySet()) {
            CtClass appEntryCls = entry.key
            String appClsDir = entry.value
            if (moduleEntryClsList != null && moduleEntryClsList.size() > 0) {
                initAppModulesMethod(appEntryCls, moduleEntryClsList)
                insertModuleAppMethods(appEntryCls)
                appEntryCls.writeFile(appClsDir)
            }
            break // shoule only has one app entry class
        }
    }

    private static void initAppModulesMethod(CtClass appEntryCls,
                                             List<CtClass> moduleAppClsList) {
        String initAppModueFunc = createInitAppModuleMethod(moduleAppClsList)
        Loger.d(TAG, "init app module func: \n" + initAppModueFunc)
        CtMethod ctNewMethod = CtNewMethod.make(initAppModueFunc, appEntryCls)
        appEntryCls.addMethod(ctNewMethod)
    }

    private static String createInitAppModuleMethod(List<CtClass> moduleAppClsList) {
        StringBuilder initAppBuilder = new StringBuilder(
                "private void ${INIT_APP_MODULE_METHOD}() {\n")
                .append("    com.github.hzhang.router.IAppModEntry modEntry;\n")
        for (CtClass ctClass : moduleAppClsList) {
            CtClass moduleSvsCls = getModuleSvsClass(ctClass)
            initAppBuilder.append("    modEntry = new ${ctClass.name}();\n")
            initAppBuilder.append("    com.github.hzhang.router.ModulesMgr.register(\n" +
                    "        " + (moduleSvsCls != null ? "${moduleSvsCls.name}.class" : "null") +
                    ", modEntry);\n")
        }
        return initAppBuilder.append("}").toString()
    }

    private static void insertModuleAppMethods(CtClass appEntryCls) {
        final List<String> methods = new ArrayList<>()
        methods.add(CREATE_METHOD)
        methods.add(ATTACH_BASE_CONTENT_METHOD)
        methods.add(TRIM_MEM_METHOD)
        methods.add(LOW_MEM_METHOD)
        for (String methodItem : methods) {
            insertNotifyMethod(appEntryCls, methodItem)
        }
    }

    private static void insertNotifyMethod(CtClass appEntryCls, final String methodName) {
        try {
            CtMethod ctMethod = appEntryCls.getDeclaredMethod(methodName)
            final String notifyMethodName = "notify${ClassUtils.camelName(methodName)}"
            if (methodName == ATTACH_BASE_CONTENT_METHOD) {
                ctMethod.insertAfter("{\n" +
                        "    ${INIT_APP_MODULE_METHOD}();\n" +
                        "    ${MODULES_MGR}.${notifyMethodName}(\$\$);\n"
                        + "}");
            } else if (methodName == CREATE_METHOD) {
                ctMethod.insertBefore("{\n" +
                        "    ${MODULES_MGR}.${notifyMethodName}(\$\$);\n" +
                        "}")
            } else {
                ctMethod.insertAfter("{\n" +
                        "    ${MODULES_MGR}.${notifyMethodName}(\$\$);\n" +
                        "}")
            }
        } catch (NotFoundException e) {
            Loger.w(TAG, "${methodName}() in ${appEntryCls.simpleName} not found and will not dispatched.\n ${e}")
        }
    }

    private static CtClass getModuleSvsClass(final CtClass moduleAppCls) {
        CtMethod svsProvMethod = moduleAppCls.getDeclaredMethod(MODULE_APP_CRETAE_SVS_PROV)
        String className = ClassUtils.getSvsClassNameFromProvMethodSig(svsProvMethod.getGenericSignature())
        Loger.d(TAG, "${moduleAppCls.name}, svsClassName: ${className}")
        CtClass moduleSvsInterfaceCls = null
        if (className != null && className.length() > 0) {
            ClassPool classPool = RouteClassPool.getClassPool()
            CtClass moduleSvsCls = classPool.get(className)
            CtClass[] moduleInterfaces = moduleSvsCls.getInterfaces()
            if (moduleInterfaces != null) {
                if (moduleInterfaces.length == 1) {
                    moduleSvsInterfaceCls = moduleInterfaces[0]
                } else {
                    for (CtClass ctClazz : moduleInterfaces) {
                        if (ctClazz.hasAnnotation(ModuleSvs.class)) {
                            moduleSvsInterfaceCls = ctClazz
                            break
                        }
                    }
                    if (moduleSvsCls == null) {
                        Loger.w(TAG, "${moduleAppCls.name} should implement module serive " +
                                "interface which should be annotated with ${ModuleSvs.class.simpleName}")
                        moduleSvsInterfaceCls = moduleInterfaces[0]
                    }
                }
            }
        }
        if (moduleSvsInterfaceCls != null) {
            Loger.d(TAG, "module api class: ${moduleSvsInterfaceCls.name}")
        }
        return moduleSvsInterfaceCls
    }
}