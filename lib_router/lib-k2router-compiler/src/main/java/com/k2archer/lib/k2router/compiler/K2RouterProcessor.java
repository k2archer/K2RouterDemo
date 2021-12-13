package com.k2archer.lib.k2router.compiler;


import com.google.auto.service.AutoService;
import com.k2archer.lib.k2router.api.K2Route;
import com.k2archer.lib.k2router.api.K2Router;
import com.k2archer.lib.k2router.api.K2Service;
import com.k2archer.lib.k2router.api.common.RouterConst;
import com.k2archer.lib.k2router.impl.K2RouterComponent;
import com.k2archer.lib.k2router.impl.K2RouterInit;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;


@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class K2RouterProcessor extends AbstractProcessor {
    private String mModelName;
    private Filer mFiler;
    private Messager mMessenger;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        //
        mMessenger = processingEnv.getMessager();
        //
        Map<String, String> options = processingEnv.getOptions();
        mModelName = options.get(RouterConst.ProcessorOptionsArguments);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }

        mMessenger.printMessage(Diagnostic.Kind.NOTE, "K2Service: " + roundEnv.getElementsAnnotatedWith(K2Service.class));
        mMessenger.printMessage(Diagnostic.Kind.NOTE, "K2Route: " + roundEnv.getElementsAnnotatedWith(K2Route.class));

        List<String> list = addAnnotationSentence(roundEnv, K2Service.class);
        list.addAll(addAnnotationSentence(roundEnv, K2Route.class));

        scanModule(list.size() > 0);

        if (list.size() == 0) {
            return true;
        }

        // 检查模块名
        mMessenger.printMessage(Diagnostic.Kind.NOTE, "mModelName: " + mModelName);

        if (mModelName == null) {
            // javaCompileOptions.annotationProcessorOptions.arguments = [moduleName: project.getName()]"
            String message = "annotationProcessorOptions.arguments 缺少 " + RouterConst.ProcessorOptionsArguments + " 键值";
            message += "\n请在 buildPost.gradle defaultConfig { } 中添加  ";
            message += "\njavaCompileOptions.annotationProcessorOptions.arguments = [moduleName: project.getName()]";
            mMessenger.printMessage(Diagnostic.Kind.ERROR, message);
            return false;
        }
        mModelName = mModelName.replaceAll("[^A-Za-z0-9_]", "_");

        String className = "Mapping_" + mModelName;
        generate_Mapping_class(className, list);

        ArrayList<String> m = new ArrayList<String>();
        loadModelSentences(m);
        for (String s : m) {
//            mMessenger.printMessage(Diagnostic.Kind.NOTE, "--ModelName: " + s);
        }
        generate_K2RouterInit(m);

        return true;
    }

    private List<String> addAnnotationSentence(RoundEnvironment roundEnv, Class<? extends Annotation> aClass) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(aClass);
        List<String> result = new ArrayList<>();
        if (elements == null || elements.isEmpty()) {
            return result;
        }

        try {
            for (Element element : elements) {
                Annotation s = element.getAnnotation(aClass);
                Method[] m = s.annotationType().getDeclaredMethods();
                String value = (String) m[0].invoke(s);

                String sentence = K2Router.class.getName() + ".getInstance().add(\"" + value + "\", " + element + ".class)";
                result.add(sentence);
                mMessenger.printMessage(Diagnostic.Kind.NOTE, K2RouterProcessor.class.getSimpleName() + ":\n" + sentence);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            mMessenger.printMessage(Diagnostic.Kind.ERROR, e.getLocalizedMessage());
            return new ArrayList<>();
        }

        return result;
    }

    private boolean generate_Mapping_class(String className, List<String> sentences) {
        String packageName = RouterConst.GENERATION_PACKAGE_NAME;

        MethodSpec.Builder init_MethodBuilder = MethodSpec.methodBuilder("init");
        init_MethodBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        for (String sentence : sentences) {
            init_MethodBuilder.addStatement(sentence);
        }

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(init_MethodBuilder.build());

        // 实现 K2RouterComponent
        classBuilder.addSuperinterface(K2RouterComponent.class);
        // 重载 onLoad()
        MethodSpec.Builder onLoad_MethodBuilder = MethodSpec.methodBuilder("onLoad");
        onLoad_MethodBuilder.addAnnotation(Override.class);
        onLoad_MethodBuilder.addModifiers(Modifier.PUBLIC);
        // 调用静态 init()
        onLoad_MethodBuilder.addStatement("init()");
        classBuilder.addMethod(onLoad_MethodBuilder.build());

        try {
            // 写入 K2RouterInit.java 文件
            JavaFile.builder(packageName, classBuilder.build()).build().writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
            mMessenger.printMessage(Diagnostic.Kind.ERROR, e.getLocalizedMessage());
            return false;
        }
//
        return true;
    }

    private boolean generate_K2RouterInit(List<String> statements) {

        MethodSpec.Builder init_MethodBuilder = MethodSpec.methodBuilder("init");
        init_MethodBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        init_MethodBuilder.addException(ClassNotFoundException.class);
        init_MethodBuilder.addException(IllegalAccessException.class);
        init_MethodBuilder.addException(InstantiationException.class);

        for (String statement : statements) {
            init_MethodBuilder.addStatement(statement);
        }

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(K2RouterInit.class.getSimpleName())
                .addModifiers(Modifier.PUBLIC)
                .addMethod(init_MethodBuilder.build());

        try {
            // 写入 K2RouterInit.java 文件
            JavaFile.builder(RouterConst.GENERATION_PACKAGE_NAME, classBuilder.build()).build().writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean scanModule(boolean hasAnnotation) {
        String moduleName = mModelName;
        // 创建临时文件
        File build = new File("build");
        if (!build.exists()) {
            if (!build.mkdir()) {
                mMessenger.printMessage(Diagnostic.Kind.ERROR, " 创建 build 目录失败");
                return false;
            }
        }
        String temp_modules_name_file_path = "build\\modules_name_temp.txt";
        File file = new File(temp_modules_name_file_path);
        try {
            if (!file.exists() && !file.createNewFile()) {
                mMessenger.printMessage(Diagnostic.Kind.ERROR, "创建临时文件失败");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Set<String> moduleNames = new HashSet<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                moduleNames.add(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            mMessenger.printMessage(Diagnostic.Kind.ERROR, "读取临时文件失败");
            return false;
        }

        try (FileWriter writer = new FileWriter(temp_modules_name_file_path, true)) {
            if (hasAnnotation) {
                if (!moduleNames.contains(moduleName)) {
                    writer.append(moduleName);  // 保存模块名称
                    writer.append("\n");
                    writer.flush();
                }
            } else {
                moduleNames.remove(moduleName);
                writer.write(""); // 清空文件
                writer.flush();
                for (String name : moduleNames) {
                    writer.append(name); // 重写
                }
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            mMessenger.printMessage(Diagnostic.Kind.ERROR, "生成临时文件失败");
            return false;
        }

        return true;
    }

    private boolean loadModelSentences(List<String> sentences) {
        String temp_modules_name_file_path = "build\\modules_name_temp.txt";
        File file = new File(temp_modules_name_file_path);
        if (file.exists()) {
            try (Scanner scanner = new Scanner(new File(temp_modules_name_file_path))) {
                while (scanner.hasNextLine()) {
                    String str = "((K2RouterComponent) Class.forName(\""
                            + RouterConst.GENERATION_PACKAGE_NAME + ".Mapping_" + scanner.nextLine()
                            + "\").newInstance()).onLoad();";
                    sentences.add(str);  // 读取模块名称，生成语句
                }
                scanner.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                mMessenger.printMessage(Diagnostic.Kind.ERROR, "读取临时文件失败");
                return false;
            }
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(K2Route.class.getCanonicalName());
        types.add(K2Service.class.getCanonicalName());
        return types;
    }

    @Override
    public Set<String> getSupportedOptions() {
        Set<String> options = new LinkedHashSet<>();
        options.add(RouterConst.ProcessorOptionsArguments);
        return options;
    }
}
