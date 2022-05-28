package com.pgj.location.processortest;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * 每个含有被注解标记的成员变量的class对应一个ClassCreatorProxy实例
 */
public class ClassCreatorProxy {
    private String mBindingClassName;
    private String mPackageName;
    private TypeElement mTypeElement;
    private Map<Integer, VariableElement> mVariableElementMap = new HashMap<>();//key为注解的value（id），value为被注解的元素

    public ClassCreatorProxy(Elements elementUtils, TypeElement classElement) {
        this.mTypeElement = classElement;
        PackageElement packageElement = elementUtils.getPackageOf(mTypeElement);
        String packageName = packageElement.getQualifiedName().toString();
        String className = mTypeElement.getSimpleName().toString();
        this.mPackageName = packageName;
        this.mBindingClassName = className + "_ViewBinding";
    }

    public void putElement(int id, VariableElement element) {
        mVariableElementMap.put(id, element);
    }

    /**
     * 创建Java代码  字符串拼接方式
     * @return
     */
    public String generateJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(mPackageName).append(";\n\n");
        builder.append("import com.example..*;\n");
        builder.append('\n');
        builder.append("public class ").append(mBindingClassName);
        builder.append(" {\n");

        generateMethods(builder);
        builder.append('\n');
        builder.append("}\n");
        return builder.toString();
    }

    /**
     * 加入Method    字符串拼接方式
     * @param builder
     */
    private void generateMethods(StringBuilder builder) {
        builder.append("public void bind(" + mTypeElement.getQualifiedName() + " host ) {\n");
        for (int id : mVariableElementMap.keySet()) {
            VariableElement element = mVariableElementMap.get(id);
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            builder.append("host." + name).append(" = ");
            builder.append("(" + type + ")(((android.app.Activity)host).findViewById( " + id + "));\n");
        }
        builder.append("  }\n");
    }

    public String getProxyClassFullName()
    {
        return mPackageName + "." + mBindingClassName;
    }

    public TypeElement getTypeElement()
    {
        return mTypeElement;
    }


    /**
     * 创建Java代码   javapoet
     * @return
     */
    public TypeSpec generateJavaCode2() {
        TypeSpec bindingClass = TypeSpec
                //class名称设置
                .classBuilder(mBindingClassName)
                //类为public
                .addModifiers(Modifier.PUBLIC)
                .addMethod(generateMethods2())
                .build();
        return bindingClass;

    }

    /**
     * 加入Method   javapoet
     */
    private MethodSpec generateMethods2() {
        ClassName host = ClassName.bestGuess(mTypeElement.getQualifiedName().toString());
        MethodSpec.Builder methodBuilder = MethodSpec
                //方法名称
                .methodBuilder("bind")
                //方法为public
                .addModifiers(Modifier.PUBLIC)
                //返回值
                .returns(void.class)
                //方法参数
                .addParameter(host, "host");

        for (int id : mVariableElementMap.keySet()) {
            VariableElement element = mVariableElementMap.get(id);
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            methodBuilder.addCode("host." + name + " = " + "(" + type + ")(((android.app.Activity)host).findViewById( " + id + "));\n");
        }
        return methodBuilder.build();
    }


    public String getPackageName() {
        return mPackageName;
    }

}