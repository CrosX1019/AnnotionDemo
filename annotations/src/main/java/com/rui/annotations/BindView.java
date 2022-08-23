package com.rui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解单独存在没有意义
 * 1. 注解+APT：用于生成一些java文件，如butterKnife、dagger2、hilt、dataBinding...
 * 2. 注解+代码埋点：如AspactJ ARouter...
 * 3. 注解+反射：如XUtils、Lifecycle...
 *
 *
 * "@Target"
 * 标识注解的生效范围，可选多个字段: {TypeA, TypeB}
 * <p>
 * "@Retention"
 * 注解的生命周期
 * RetentionPolicy.SOURCE     源码期：.java编译为.class后忽略
 * RetentionPolicy.CLASS      编译期：虚拟机加载.class文件时会忽略
 * RetentionPolicy.RUNTIME    运行期：一直可以生效
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BindView {
    // 如果只有一个方法，则注解使用时需在后方添加对应类型参数
    String value();
    // exp:
    // @BindView("value")
    // private String str;

    // 如果含有多个方法，则需要添加对应方法名的参数
    int id();
    // exp:
    // @BindView(value = "value", id = 1)
    // private String str;
}
