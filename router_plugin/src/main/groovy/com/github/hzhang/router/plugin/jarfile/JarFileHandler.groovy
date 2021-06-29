package com.github.hzhang.router.plugin.jarfile


import com.github.hzhang.router.plugin.util.PathUtils
import com.github.hzhang.router.plugin.log.Loger
import javassist.CtClass

import javax.annotation.Nonnull
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class JarFileHandler {
    private static final String TAG = "JarFileHandler"

    static void writeJarFile(@Nonnull JarFile jarFile,
                             @Nonnull File dest,
                             CtClass modifiedCls) {
        Manifest manifest = jarFile.getManifest()
        JarOutputStream jarOutputStream = manifest == null
                ? new JarOutputStream(new FileOutputStream(dest))
                : new JarOutputStream(new FileOutputStream(dest), jarFile.getManifest())
        try {
            String modifiedClsName = modifiedCls != null ? modifiedCls.name : null
            jarFile.stream().forEach() {
                jarOutputStream.putNextEntry(new JarEntry(it.name))
                String className = PathUtils.classNameFromPath(it.name)
                if (modifiedCls != null && className == modifiedClsName) {
                    Loger.d(TAG, "write modified class to jarfile: ${jarFile.name}")
                    jarOutputStream.write(modifiedCls.toBytecode())
                } else {
                    writeJarEntry(jarOutputStream, jarFile.getInputStream(it))
                }
                jarOutputStream.closeEntry()
            }
        } catch (Exception e) {
            Loger.e(TAG, "write jar file error: " + e)
            throw e
        } finally {
            jarOutputStream.flush()
            jarOutputStream.close()
        }
    }

    private static void writeJarEntry(JarOutputStream jarOutputStream, InputStream inputStream) {
        if (jarOutputStream != null && inputStream != null) {
            try {
                byte[] byteBuf = new byte[2048];
                int size
                while ((size = inputStream.read(byteBuf)) > 0) {
                    jarOutputStream.write(byteBuf, 0, size)
                }
            } catch (Exception e) {
                Loger.e(TAG, "writeJarEntry: ${e}")
            } finally {
                inputStream.close()
            }
        }
    }
}