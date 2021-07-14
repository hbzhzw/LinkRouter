package com.github.hzhang.router;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.github.hzhang.router.anno.DefaultRet;
import com.github.hzhang.router.anno.ModuleSvs;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@SuppressWarnings({"unused", "RedundantSuppression"})
@SupportedAnnotationTypes("com.github.hzhang.router.anno.ModuleSvs")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class RouteAnnoProcessor extends AbstractProcessor {
    private static final String TAG = "RouteAnnoProcessor";
    private static final String ANNO_CLS = "com.github.hzhang.router.anno.ModuleSvs";
    private static final String MODULE_SERVICE_API_PREFIX = "I";
    private static final String MODULE_SERVICE_API_POSTFIX = "Service";
    private static final String MODULE_API_CLS_POSTFIX = "ApiMgr";

    private static final ClassName ROUTE_MODULE_MGR
            = ClassName.bestGuess("com.github.hzhang.router.ModulesMgr");


    // @Override
    // public synchronized void init(ProcessingEnvironment processingEnvironment) {
    //     super.init(processingEnvironment);
    //     logI("init RouteAnnoProcessor");
    // }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.size() <= 0) {
            return false;
        }
        for (TypeElement ele : set) {
            logI("process annotation: " + ele);
            Set<? extends Element> moduleSvsEles
                    = roundEnvironment.getElementsAnnotatedWith(ele);
            if (moduleSvsEles != null && moduleSvsEles.size() > 0) {
                for (Element moduleEle : moduleSvsEles) {
                    // logI("moduleEle: " + moduleEle);
                    if (moduleEle.getKind() == ElementKind.INTERFACE) {
                        Elements elementsUtil = processingEnv.getElementUtils();
                        String packageName = elementsUtil.getPackageOf(moduleEle)
                                .getQualifiedName().toString();
                        String clsName = moduleEle.getSimpleName().toString();
                        String apiClsName = getApiClsName(moduleEle);
                        TypeSpec.Builder apiTypeBuilder = TypeSpec.classBuilder(apiClsName)
                                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                .addJavadoc("Generated Code. Do not edit it!\n" +
                                                "see annotation {@link $L}\n" +
                                                "see interface {@link $L}\n",
                                        ele.getQualifiedName().toString(),
                                        clsName);
                        List<? extends Element> enclosedEles = moduleEle.getEnclosedElements();
                        for (Element enclosedEle : enclosedEles) {
                            if (enclosedEle.getKind() == ElementKind.METHOD) {
                                Symbol.MethodSymbol methodSymbol
                                        = (Symbol.MethodSymbol) enclosedEle;
                                apiTypeBuilder.addMethod(
                                        buildMethod((TypeElement)moduleEle, methodSymbol));
                            }
                        }
                        try {
                            JavaFile.builder(packageName, apiTypeBuilder.build())
                                    .build()
                                    .writeTo(processingEnv.getFiler());
                            logI("generate api class mgr: " + packageName + "." + apiClsName);
                        } catch (Exception e) {
                            logE(e.toString());
                        }
                    } else {
                        logE("the annotated element(" + moduleEle.getSimpleName()
                                + ") is not an interface");
                    }
                }
            }
            logI("ele: " + ele.getQualifiedName());
        }
        return false;
    }

    private MethodSpec buildMethod(TypeElement apiClsEle, Symbol.MethodSymbol methodSymbol) {
        logI("methodName: " + methodSymbol.name + ", simpleName: " + methodSymbol.getSimpleName());
        Type retType = methodSymbol.getReturnType();
        String methodName = methodSymbol.getSimpleName().toString();
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .returns(TypeName.get(retType));
        List<Symbol.VarSymbol> argSymbols = methodSymbol.getParameters();
        if (argSymbols != null && argSymbols.size() > 0) {
            for (Symbol.VarSymbol argSymbol : argSymbols) {
                methodSpecBuilder.addParameter(ParameterSpec.builder(TypeName.get(argSymbol.type),
                        argSymbol.name.toString()).build());
            }
        }
        ClassName apiClassName = ClassName.get(apiClsEle);
        methodSpecBuilder.addStatement("$T t$L = $T.service($T.class)",
                apiClassName,
                apiClsEle.getSimpleName(),
                ROUTE_MODULE_MGR,
                apiClassName);
        //noinspection SwitchStatementWithTooFewBranches
        switch (retType.getKind()) {
            case BOOLEAN:
                methodSpecBuilder.addStatement(
                        "return t$L != null && t$L.$L($L)",
                        apiClsEle.getSimpleName(),
                        apiClsEle.getSimpleName(),
                        methodName,
                        argSymbols);
                break;

            case VOID:
                methodSpecBuilder.beginControlFlow(
                        "if (t$L != null)", apiClsEle.getSimpleName())
                        .addStatement("t$L.$L($L)",
                                apiClsEle.getSimpleName(),
                                methodName,
                                argSymbols)
                        .endControlFlow();
                break;

            default:
                methodSpecBuilder.addStatement(
                        "return t$L != null ? t$L.$L($L) : $L",
                        apiClsEle.getSimpleName(),
                        apiClsEle.getSimpleName(),
                        methodName,
                        argSymbols,
                        getDefaultRetValue(methodSymbol, retType));
                break;
        }
        return methodSpecBuilder.build();
    }

    private String getDefaultRetValue(Symbol.MethodSymbol methodSymbol,
            final Type retType) {
        String defaultRet;
        DefaultRet defRet = methodSymbol.getAnnotation(DefaultRet.class);
        switch (retType.getKind()) {
            case INT:
            case SHORT:
            case BYTE:
            case FLOAT:
            case DOUBLE:
                defaultRet = defRet != null ? defRet.value() : "-1";
                break;
            default:
                defaultRet = defRet != null ? defRet.value() : "null";
                break;
        }
        return defaultRet;
    }

    private String getApiClsName(Element typeEle) {
        ModuleSvs moduleSvs = typeEle.getAnnotation(ModuleSvs.class);
        String apiClsName = moduleSvs.value();
        apiClsName = apiClsName.trim();
        if (apiClsName.length() <= 0) {
            apiClsName = typeEle.getSimpleName().toString();
            if (apiClsName.startsWith(MODULE_SERVICE_API_PREFIX)) {
                apiClsName = apiClsName.substring(MODULE_SERVICE_API_PREFIX.length());
            }
            if (apiClsName.endsWith(MODULE_SERVICE_API_POSTFIX)) {
                int idx = apiClsName.lastIndexOf(MODULE_SERVICE_API_POSTFIX);
                apiClsName = apiClsName.substring(0, idx);
            }
            apiClsName += MODULE_API_CLS_POSTFIX;
        }
        return apiClsName;
    }

    private void logI(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                TAG + ": " + msg);
    }

    private void logW(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                TAG + ": " + msg);
    }

    private void logE(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                TAG + ": " + msg);
    }
}