package com.rui.annotation_compiler;

import com.google.auto.service.AutoService;
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
                if(viewElementList == null) {
                    viewElementList = new ArrayList<>();
                }
            }
            viewElementList.add(variableElement);
            elementForType.setViewElementList(viewElementList);
            map.put(typeElement, elementForType);
        }

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
                if(methodElementList == null) {
                    methodElementList = new ArrayList<>();
                }
            }
            methodElementList.add(executableElement);
            elementForType.setMethodElementList(methodElementList);
            map.put(typeElement, elementForType);
        }

        return map;
    }

    public void writeInFiler(Map<TypeElement, ElementForType> map) {
        if (map.size() > 0) {
            Writer writer = null;
            Iterator<TypeElement> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                //得到包名
                TypeElement typeElement = iterator.next();
                String activityName = typeElement.getSimpleName().toString();
                String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).toString();

                try {
                    JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + activityName + "_ViewBinding");

                    writer = sourceFile.openWriter();

                    writer.write("package " + packageName + ";\n");

                    writer.write("import " + packageName + ".IBinder;\n\n");

                    writer.write("import android.view.View;\n\n");

                    writer.write("public class " + activityName + "_ViewBinding implements IBinder<" + packageName + "." + activityName + "> {\n\n");

                    writer.write("    @Override\n" + "    public void bind(" + packageName + "." + activityName + " target) {\n");

                    List<VariableElement> variableElementList = map.get(typeElement).getViewElementList();
                    if (variableElementList == null || variableElementList.size() == 0) {
                        continue;
                    }
                    for (VariableElement variableElement : variableElementList) {
                        //得到名字
                        String variableName = variableElement.getSimpleName().toString();
                        //得到ID
                        int id = variableElement.getAnnotation(BindView.class).value();
                        //得到类型
                        TypeMirror typeMirror = variableElement.asType();
                        writer.write("        target." + variableName + " = (" + typeMirror + ")target.findViewById(" + id + ");\n");
                    }

                    List<ExecutableElement> executableElementList = map.get(typeElement).getMethodElementList();
                    if (executableElementList == null || executableElementList.size() == 0) {
                        continue;
                    }
                    for (ExecutableElement executableElement : executableElementList) {
                        //得到ID数组
                        int[] ids = executableElement.getAnnotation(OnClick.class).value();
                        String methodName = executableElement.getSimpleName().toString();
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int id : ids) {
                            stringBuilder.append("        target.findViewById(").append(id).append(").setOnClickListener(new View.OnClickListener() {\n");
                            stringBuilder.append("            public void onClick(View view) {\n");
                            stringBuilder.append("                target.").append(methodName).append("(view);\n");
                            stringBuilder.append("            }\n");
                            stringBuilder.append("        });\n");
                        }

                        writer.write(stringBuilder.toString());
                    }

                    writer.write("    }\n");
                    writer.write("}");


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


    public void showLog(String log) {
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, log);
    }
}