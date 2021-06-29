package com.github.hzhang.router.plugin.bcode

import com.android.build.api.transform.TransformInput
import com.github.hzhang.router.plugin.log.Loger
import javassist.ClassPool
import javassist.CtClass

class RouteClassPool extends ClassPool {
    private static final String TAG = "RouteClassPool"
    private static ClassPool mClassPool = null
    private static ClassPoolBuilder mPoolBuilder

    static void clearClassPool() {
        mClassPool = null
        mPoolBuilder = null
        Loger.i(TAG, "clear class pool")
    }

    static void init(List<File> bootClassPath, Collection<TransformInput> inputs) {
        mPoolBuilder = new ClassPoolBuilder() {
            @Override
            ClassPool build() {
                ClassPool classPool = new RouteClassPool()
                Loger.i(TAG, "classpath: ")
                bootClassPath.forEach() {
                    Loger.d(TAG, "${it.path}")
                    classPool.appendClassPath(it.path)
                }
                inputs.stream().forEach() {
                    it.directoryInputs.forEach() {
                        Loger.d(TAG, "dir input: ${it.file.path}")
                        classPool.appendClassPath(it.file.path)
                    }

                    it.jarInputs.forEach() {
                        Loger.d(TAG, "jar input: ${it.file.path}")
                        classPool.appendClassPath(it.file.path)
                    }
                }
                //                inputs.parallelStream().flatMap() {
//                    it.directoryInputs.parallelStream()
//                }.filter() {
//                    it.file.exists()
//                }.forEach() {
//                    Loger.i(TAG, "${it.file.path}")
//                    classPool.appendClassPath(it.file.path)
//                }
//
//                inputs.parallelStream().flatMap( ) {
//                    it.jarInputs.parallelStream()
//                }.filter() {
//                    it.file.exists()
//                }.forEach() {
//                    Loger.i(TAG, "${it.file.path}")
//                    classPool.appendClassPath(it.file.path)
//                }
//                Loger.d(TAG, "classPool: ${classPool}");
                return classPool
            }
        }
    }

    static ClassPool getClassPool() {
        if (mClassPool == null) {
            mClassPool = mPoolBuilder.build()
        }
        return mClassPool
    }

    static defrost(CtClass ctClass) {
        if (ctClass != null && ctClass.isFrozen()) {
            ctClass.defrost()
        }
    }

    interface ClassPoolBuilder {
        ClassPool build()
    }
}