package com.rui.annotation_compiler;

import com.google.auto.service.AutoService;
import com.rui.annotations.BindString;
import com.rui.annotations.BindView;
import com.rui.annotations.OnClick;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * 注解处理程序，用来生成其他代码的
 */

@AutoService(Processor.class)//注册注解处理器
public class AnnotationCompiler extends AbstractProcessor {

    /* 注解处理器配置 */

    //1.声明Java支持的版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    //2.能用来处理哪些注解
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindView.class.getCanonicalName());
        types.add(OnClick.class.getCanonicalName());
        types.add(BindString.class.getCanonicalName());
        return types;
    }

    //3.定义一个用来生成APT目录文件下面的对象
    Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //set中存放了
        //TypeElement: 类
        //ExecutableElement: 方法
        //VariableElement: 属性

        Map<TypeElement, ElementForType> map = findAndParserTarget(roundEnvironment);
        writeInFiler(map);

        return false;
    }

    public Map<TypeElement, ElementForType> findAndParserTarget(RoundEnvironment roundEnvironment) {
        Map<TypeElement, ElementForType> map = new HashMap<>();

        //获取APP中所有用到了BindView注解的成员变量View的节点对象
        Set<? extends Element> viewElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);

        //获取APP中所有用到了OnClick注解的方法节点对象
        Set<? extends Element> methodElements = roundEnvironment.getElementsAnnotatedWith(OnClick.class);

        //获取APP中所有用到了BindString注解的成员变量String的节点对象
        Set<? extends Element> stringElements = roundEnvironment.getElementsAnnotatedWith(BindString.class);

        //保存所有BindView节点到map
        saveBindViewElements(viewElements, map);

        //保存所有OnClick节点到map
        saveOnClickElements(methodElements, map);

        //保存所有BindString节点到map
        saveBindStringElements(stringElements, map);

        return map;
    }

    public void writeInFiler(Map<TypeElement, ElementForType> map) {
        if (map.size() > 0) {
            Writer writer = null;
            for (TypeElement typeElement : map.keySet()) {
                StringBuilder stringBuilder = new StringBuilder();
                //得到包名
                String activityName = typeElement.getSimpleName().toString();
                String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).toString();
                ElementForType elementForType = map.get(typeElement);

                try {
                    JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + activityName + "_ViewBinding");

                    writer = sourceFile.openWriter();

                    stringBuilder.append("package ").append(packageName).append(";\n");

                    stringBuilder.append("import ").append(packageName).append(".IBinder;\n\n");

                    stringBuilder.append("import android.view.View;\n\n");

                    stringBuilder.append("public class ").append(activityName).append("_ViewBinding implements IBinder<").append(packageName).append(".").append(activityName).append("> {\n\n");

                    stringBuilder.append("    @Override\n" + "    public void bind(").append(packageName).append(".").append(activityName).append(" target) {\n");

                    appendBindView(elementForType, stringBuilder);

                    appendOnClick(elementForType, stringBuilder);

                    appendBindString(elementForType, stringBuilder);

                    stringBuilder.append("    }\n");
                    stringBuilder.append("}");

                    writer.write(stringBuilder.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }

    public void saveBindViewElements(Set<? extends Element> viewElements, Map<TypeElement, ElementForType> map) {
        //遍历所有成员变量View的节点，使其与之类节点一一对应
        for (Element viewElement : viewElements) {
            //cast为成员变量节点
            VariableElement variableElement = (VariableElement) viewElement;
            //获取到该成员变量的类节点
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();

            ElementForType elementForType;
            List<VariableElement> viewElementList;

            if (!map.containsKey(typeElement)) {
                elementForType = new ElementForType();
                viewElementList = new ArrayList<>();
            } else {
                elementForType = map.get(typeElement);
                viewElementList = elementForType.getViewElementList();
                if (viewElementList == null) {
                    viewElementList = new ArrayList<>();
                }
            }
            viewElementList.add(variableElement);
            elementForType.setViewElementList(viewElementList);
            map.put(typeElement, elementForType);
        }
    }

    public void saveOnClickElements(Set<? extends Element> methodElements, Map<TypeElement, ElementForType> map) {
        //遍历所有成员变量View的节点，使其与之类节点一一对应
        for (Element methodElement : methodElements) {
            //cast为成员变量节点
            ExecutableElement executableElement = (ExecutableElement) methodElement;
            //获取到该成员变量的类节点
            TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();

            ElementForType elementForType;
            List<ExecutableElement> methodElementList;

            if (!map.containsKey(typeElement)) {
                elementForType = new ElementForType();
                methodElementList = new ArrayList<>();

            } else {
                elementForType = map.get(typeElement);
                methodElementList = elementForType.getMethodElementList();
                if (methodElementList == null) {
                    methodElementList = new ArrayList<>();
                }
            }
            methodElementList.add(executableElement);
            elementForType.setMethodElementList(methodElementList);
            map.put(typeElement, elementForType);
        }
    }

    public void saveBindStringElements(Set<? extends Element> stringElements, Map<TypeElement, ElementForType> map) {
        //遍历所有成员变量String的节点，使其与之类节点一一对应
        for (Element stringElement : stringElements) {
            //cast为成员变量节点
            VariableElement variableElement = (VariableElement) stringElement;
            //获取到该成员变量的类节点
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();

            ElementForType elementForType;
            List<VariableElement> stringElementList;

            if (!map.containsKey(typeElement)) {
                elementForType = new ElementForType();
                stringElementList = new ArrayList<>();
            } else {
                elementForType = map.get(typeElement);
                stringElementList = elementForType.getStringElementList();
                if (stringElementList == null) {
                    stringElementList = new ArrayList<>();
                }
            }
            stringElementList.add(variableElement);
            elementForType.setStringElementList(stringElementList);
            map.put(typeElement, elementForType);
        }
    }

    public void appendBindView(ElementForType elementForType, StringBuilder builder) {
        List<VariableElement> variableElementList = elementForType.getViewElementList();
        if (variableElementList == null || variableElementList.size() == 0) {
            return;
        }
        for (VariableElement variableElement : variableElementList) {
            //得到名字
            String variableName = variableElement.getSimpleName().toString();
            //得到ID
            int id = variableElement.getAnnotation(BindView.class).value();
            //得到类型
            TypeMirror typeMirror = variableElement.asType();
            builder.append("        target.").append(variableName).append(" = (").append(typeMirror).append(")target.findViewById(").append(id).append(");\n\n");
        }
    }

    public void appendOnClick(ElementForType elementForType, StringBuilder builder) {
        List<ExecutableElement> executableElementList = elementForType.getMethodElementList();
        if (executableElementList == null || executableElementList.size() == 0) {
            return;
        }
        for (ExecutableElement executableElement : executableElementList) {
            //得到ID数组
            int[] ids = executableElement.getAnnotation(OnClick.class).value();
            String methodName = executableElement.getSimpleName().toString();
            for (int id : ids) {
                builder.append("        target.findViewById(").append(id).append(").setOnClickListener(new View.OnClickListener() {\n");
                builder.append("            public void onClick(View view) {\n");
                builder.append("                target.").append(methodName).append("(view);\n");
                builder.append("            }\n");
                builder.append("        });\n\n");
            }
        }
    }

    public void appendBindString(ElementForType elementForType, StringBuilder builder) {
        List<VariableElement> variableElementList = elementForType.getStringElementList();
        if (variableElementList == null || variableElementList.size() == 0) {
            return;
        }
        for (VariableElement variableElement : variableElementList) {
            //得到名字
            String variableName = variableElement.getSimpleName().toString();
            //得到ID
            int id = variableElement.getAnnotation(BindString.class).value();
            builder.append("        target.").append(variableName).append(" = target.getResources().getString(").append(id).append(");\n\n");
        }
    }


    public void showLog(String log) {
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, log);
    }
}