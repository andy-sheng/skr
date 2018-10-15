package com.qihoo360.replugin.gradle.plugin;

import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class JavassistTest {

    private String add(String aaa,String bbb){
        return aaa+bbb;
    }

    private String jisuan(){
        String a = add("android","hahaha");
        return a;
    }

    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("com.qihoo360.replugin.gradle.plugin.JavassistTest");
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.get(clazz.getName());
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method mt : declaredMethods) {
            String modifier = Modifier.toString(mt.getModifiers());
            Class<?> returnType = mt.getReturnType();
            String name = mt.getName();
            Class<?>[] parameterTypes = mt.getParameterTypes();
            System.out.print("\n modifier:" + modifier + " returnType:" + returnType.getName() + " name:" + name + " (");


            CtMethod ctm = cc.getDeclaredMethod(name);
            MethodInfo methodInfo = ctm.getMethodInfo();
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
            LocalVariableAttribute attribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
            int pos = Modifier.isStatic(ctm.getModifiers()) ? 0 : 1;
            for (int i = 0; i < ctm.getParameterTypes().length; i++) {
                System.out.print(parameterTypes[i] + " " + attribute.variableName(i + pos));
                if (i < ctm.getParameterTypes().length - 1) {
                    System.out.print(",");
                }
            }
            System.out.print(")");
            Class<?>[] exceptionTypes = mt.getExceptionTypes();
            if (exceptionTypes.length > 0) {
                System.out.print(" throws ");
                int j = 0;
                for (Class<?> cl : exceptionTypes) {
                    System.out.print(cl.getName());
                    if (j < exceptionTypes.length - 1) {
                        System.out.print(",");
                    }
                    j++;
                }
            }
        }
    }
}
