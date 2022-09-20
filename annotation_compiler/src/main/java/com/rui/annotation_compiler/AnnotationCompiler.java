package com.rui.annotation_compiler;

import com.google.auto.service.AutoService;
import com.rui.annotations.BindView;

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
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * 注解处理程序，用来生成其他代码的
 */

@AutoService(Process.class)//注册注解处理器
public class AnnotationCompiler extends AbstractProcessor {

    /* 注解处理器配置 */

    //1.支持的版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    //2.能用来处理哪些注解
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindView.class.getCanonicalName());
//        types.add(Override.class.getCanonicalName());
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
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Test: " + set);

        //set中存放了
        //TypeElement: 类
        //ExecutableElement: 方法
        //VariableElement: 属性

        //获取APP中所有用到了BIndView注解的对象
        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(BindView.class);

        //对elementsAnnotatedWith进行分类
        Map<String, List<VariableElement>> map = new HashMap<>();
        for (Element element : elementsAnnotatedWith) {
            VariableElement variableElement = (VariableElement) element;
            //variableElement.getEnclosingElement()获取包裹element的上层类
            String activityName = variableElement.getEnclosingElement().getSimpleName().toString();
            List<VariableElement> variableElementList = map.get(activityName);
            if (variableElementList == null) {
                variableElementList = new ArrayList<>();
                map.put(activityName, variableElementList);
            }
            variableElementList.add(variableElement);
        }

        if (map.size() > 0) {
            Writer writer = null;
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                //key
                String activityName = iterator.next();
                //value
                List<VariableElement> variableElementList = map.get(activityName);
                //得到包名
                TypeElement enclosingElement = (TypeElement) variableElementList.get(0).getEnclosingElement();
                String packageName = processingEnv.getElementUtils().getPackageOf(enclosingElement).toString();

                try {
                    JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + activityName + "_ViewBinding");

                    writer = sourceFile.openWriter();

                    writer.write("package " + packageName + ";\n");

                    writer.write("import " + packageName + ".IBinder;\n");

                    writer.write("public class " + activityName + "_ViewBinding implements IBinder<" + packageName + "." + activityName + "> {\n");

                    writer.write("  @Override\n" + "    public void bind(" + packageName + "." + activityName + "target){");

                    for (VariableElement variableElement : variableElementList) {
                        //得到名字
                        String variableName = variableElement.getSimpleName().toString();
                        //得到ID
                        int id = variableElement.getAnnotation(BindView.class).value();
                        //得到类型
                        TypeMirror typeMirror = variableElement.asType();

                        writer.write("target." + variableName + " = (" + typeMirror + ")target.findViewById(" + id + ");\n");
                    }

                    writer.write("\n}}");


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



        return false;
    }
}