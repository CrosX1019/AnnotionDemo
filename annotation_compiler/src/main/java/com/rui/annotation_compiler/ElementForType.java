package com.rui.annotation_compiler;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

public class ElementForType {

    //所有绑定View的成员变量的节点集合
    List<VariableElement> viewElementList;

    //所有绑定方法的节点集合
    List<ExecutableElement> methodElementList;

    public List<VariableElement> getViewElementList() {
        return viewElementList;
    }

    public void setViewElementList(List<VariableElement> viewElementList) {
        this.viewElementList = viewElementList;
    }

    public List<ExecutableElement> getMethodElementList() {
        return methodElementList;
    }

    public void setMethodElementList(List<ExecutableElement> methodElementList) {
        this.methodElementList = methodElementList;
    }
}
