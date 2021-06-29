package com.github.hzhang.router.plugin.transform

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.github.hzhang.router.plugin.bcode.CodeGenerateMgr
import com.github.hzhang.router.plugin.bcode.RouteClassPool
import com.github.hzhang.router.plugin.log.Loger
import javassist.CtClass
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class RouteTransform extends Transform {
    private static final String TRANSF_NAME = "RouteTransform"
    private static final String TAG = TRANSF_NAME
    private final Project mProj
    private Map<CtClass, String> mAppClsEntryMap = new HashMap<>(6)
    private List<CtClass> mModuleEntryClsList = new ArrayList<>()

    RouteTransform(Project project) {
        mProj = project
    }

    @Override
    String getName() {
        return TRANSF_NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isCacheable() {
        return false
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transInvo) throws TransformException,
            InterruptedException, IOException {
        long startTime = System.currentTimeMillis()
        Loger.i(TAG, "do transform starts, incremental: " + transInvo.incremental)
        try {
            Collection<TransformInput> inputs = transInvo.getInputs()
            initClassPool(inputs)
            TransformOutputProvider oProvider = transInvo.getOutputProvider()
            inputs.forEach() {
                transformDirInputAndFindAppEntry(it.directoryInputs, oProvider)
                transformJarInput(it.jarInputs, oProvider)
            }
            CodeGenerateMgr.generateEntryCode(mAppClsEntryMap, mModuleEntryClsList)
        } catch (Exception e) {
            Loger.e(TAG, "exception: ${e}")
        } finally {
            clearClassInfo()
        }
        long endTime = System.currentTimeMillis()
        Loger.i(TAG, "do transform ends, time cost: ${endTime - startTime}ms")
    }

    private void initClassPool(Collection<TransformInput> inputs) {
        AppExtension android = mProj.extensions.getByType(AppExtension)
        RouteClassPool.init(android.getBootClasspath(), inputs)
    }

    private void clearClassInfo() {
        mAppClsEntryMap.clear()
        mModuleEntryClsList.clear()
        RouteClassPool.clearClassPool()
    }

    private void transformDirInputAndFindAppEntry(
            Collection<DirectoryInput> dirInputs,
            TransformOutputProvider oProvider) {
        if (dirInputs != null && dirInputs.size() > 0) {
            for (DirectoryInput dirInput : dirInputs) {
                File dest = oProvider.getContentLocation(dirInput.name,
                        dirInput.contentTypes,
                        dirInput.scopes,
                        Format.DIRECTORY)
                Loger.d(TAG, "dir input: ${dirInput.name}, dir file path: ${dirInput.file.path}" +
                        ", contentTypes: ${dirInput.contentTypes}" +
                        ", scopes: ${dirInput.scopes}, file: ${dirInput.file}" +
                        ", changedFiles: ${dirInput.changedFiles}" +
                        ", ouputPath: ${dest.path}")
                FileUtils.copyDirectory(dirInput.file, dest)
                CtClass appEntryCls = CodeGenerateMgr.findAppEntryCls(dest)
                if (appEntryCls != null) {
                    mAppClsEntryMap.put(appEntryCls, dest.path)
                }
            }
            if (mAppClsEntryMap.size() > 1) {
                Loger.e(TAG, "you have too many app entry classes annotated with AppEntry: ${mAppClsEntryMap}")
            }
        }
    }

    private void transformJarInput(Collection<JarInput> jarInputs,
                                   TransformOutputProvider oProvider) {
        jarInputs.forEach() {
            File dest = oProvider.getContentLocation(it.name,
                    it.contentTypes,
                    it.scopes,
                    Format.JAR)
            Loger.d(TAG, "jarInput name: ${it.name}" +
                    ", status: ${it.status}" +
                    ", scopes: ${it.scopes}" +
                    ", contentTypes: ${it.contentTypes}" +
                    ", path: ${it.file.path}" +
                    ", dest: ${dest.path}")
            CtClass moduleAppCls = CodeGenerateMgr.transformModuleJar(it.file, dest)
            if (moduleAppCls != null) {
                mModuleEntryClsList.add(moduleAppCls)
            }
        }
    }
}