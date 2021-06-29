package com.github.hzhang.router.plugin.util

import javassist.ClassPool

import javax.annotation.Nonnull

class PathUtils {
    private static final String CLASS_EXT = ".class"
//    private static final String JAVA_PACK_PREFIX = CLASS_STR + "es" + File.separator

    static String classNameFromPath(String filePath) {
        String className = null
        int idx = filePath != null ? filePath.lastIndexOf(CLASS_EXT) : -1
        if (idx >= 0) {
            className = filePath.substring(0, filePath.length() - CLASS_EXT.length())
            className = pathToClass(className)
        }
        return className
    }

    static String pathToClass(String filePath) {
        return filePath != null ? filePath.replaceAll(File.separator, ".") : null
    }

    static String baseName(String filePath) {
        String baseName = filePath;
        if (filePath != null && filePath.length() > 0) {
            int sIdx = filePath.lastIndexOf(File.separator)
            if (sIdx >= 0) {
                baseName = filePath.substring(++sIdx)
            }
        }
        return baseName
    }

    static String classNameFromPath(String filePath, int prefixRemove) {
        String className = null
        prefixRemove = Math.max(0, prefixRemove)
        if (filePath != null && filePath.length() > 0) {
            className = filePath.substring(prefixRemove, filePath.length() - CLASS_EXT.length())
            className = pathToClass(className)
        }
        return className
    }

//    static String getClassPath(String filePath) {
//        String classDir = null;
//        int idx = filePath != null ? filePath.indexOf(JAVA_PACK_PREFIX) : -1
//        if (idx >= 0) {
//            classDir = filePath.substring(0,
//                    idx + JAVA_PACK_PREFIX.length() - File.separator.length())
//        }
//        return classDir
//    }

    static boolean insertClassPath(@Nonnull ClassPool classPool, String classPath) {
        boolean  isSuccess = false
        if (classPath != null && classPath.length() > 0) {
            classPool.insertClassPath(classPath)
            isSuccess = true
        }
        return isSuccess
    }
}