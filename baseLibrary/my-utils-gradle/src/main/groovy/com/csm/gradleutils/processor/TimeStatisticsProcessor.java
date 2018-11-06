package com.csm.gradleutils.processor;


import com.csm.gradleutils.utils.U;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;


public class TimeStatisticsProcessor extends BaseProcessor {

    String[] cls = new String[]{
            "Activity",
            "Fragment",
            "View"
    };

    boolean needMonitor(String className) {
        for (String s : cls) {
            if (className.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void processClass(CtClass ctClass) {
        boolean isAbstract = Modifier.isAbstract(ctClass.getModifiers());
        if (isAbstract) {
            return;
        }
        if (ctClass.getName().endsWith("com.common.statistics.TimeStatistics")) {
            U.print(100, "修改" + ctClass.getName() + "的配置");
            for (CtMethod ctMethod : ctClass.getMethods()) {
                if (ctMethod.getName().endsWith("getSwitch")) {
                    try {
                        ctMethod.insertBefore("if(true){" +
                                "return true;" +
                                "}");
                    } catch (CannotCompileException e) {
                        e.printStackTrace();
                    }
                }
            }
//            try {
//                CtField ctField = ctClass.getme("sClose");
//                U.print(100, "1111的配置");
//                ctClass.removeField(ctField);
//                U.print(100, "2222的配置");
//                CtField ctNewField = CtField.make("public static boolean sClose = false;", ctClass);
//                U.print(100, "3333的配置");
//                ctClass.addField(ctNewField);
//                U.print(100, "4444的配置");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            return;
        }
        boolean needMonitor = needMonitor(ctClass.getName());
        CtClass superCls = ctClass;
        while (!needMonitor) {
            try {
                superCls = superCls.getSuperclass();
            } catch (NotFoundException e) {
                e.printStackTrace();
                superCls = null;
            }
            if (superCls != null && !superCls.getName().endsWith("java.lang.Object")) {
                U.print(1, "ctClass" + ctClass.getName() + " superCls:" + superCls.getName());
                needMonitor = needMonitor(superCls.getName());
            } else {
                break;
            }
        }

        if (needMonitor) {
            processInner(ctClass);
        }
    }

    void processInner(CtClass ctClass) {
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            U.print(1, "ctMethod:" + ctMethod.getName());
            if (ctMethod.getName().endsWith("access$")) {
                continue;
            }
            int modifiers = ctMethod.getModifiers();
            boolean isStatic = Modifier.isStatic(modifiers);
            String c = null;
            if (isStatic) {
                c = ctClass.getName() + ".class.getSimpleName()";
            } else {
                c = "$0.getClass().getSimpleName()";
            }
            try {
                ctMethod.insertBefore("com.common.statistics.TimeStatistics.setBeginTime(" + c + ",\"" + ctMethod.getName() + "\");");
                ctMethod.insertAfter("com.common.statistics.TimeStatistics.setEndTime(" + c + ",\"" + ctMethod.getName() + "\");");
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        }
    }

}
