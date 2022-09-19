package com.rui.annotiondemo;

/**
 * 用来绑定Activity
 */
public interface IBinder<T> {
    void bind(T target);
}
