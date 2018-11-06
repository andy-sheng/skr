package com.csm.gradleutils.processor;


import com.csm.gradleutils.utils.U;

import java.lang.reflect.Constructor;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;


/**
 * 修改第三库里的一个参数
 * 由 ARGB_8888 变为 ARGB_4444
 * 减小泄漏风险
 */
public class InjectGifProcessor extends BaseProcessor {


    ExprEditor ed = new ExprEditor() {
        @Override
        public void edit(MethodCall m) throws CannotCompileException {
            String className = m.getClassName();
            String methodName = m.getMethodName();
            U.print(100, "statement className:" + className + " methodName:" + methodName);
            if (className.endsWith("android.graphics.Bitmap")
                    && methodName.endsWith("createBitmap")) {
                m.replace("{ " +
                        "$3=android.graphics.Bitmap.Config.ARGB_4444;" +
                        "$_ = $proceed($$);" +
                        " }");
            }
        }
    };

    @Override
    public void processClass(CtClass ctClass) throws CannotCompileException {
        String injectClassName = "pl.droidsonroids.gif.GifDrawable";
        if (ctClass.getName().endsWith(injectClassName)) {
            U.print(100, "找到class:" + ctClass.getName());
            for (CtConstructor ctConstructor : ctClass.getConstructors()) {
                U.print(100, "ctConstructor:" + ctConstructor.getName());
                ctConstructor.instrument(ed);
            }
            for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                U.print(100, "ctMethod:" + ctMethod.getName());
                if (ctMethod.getName().endsWith("GifDrawable")) {
                    U.print(100, "找到method:" + ctMethod.getName());
                    ctMethod.instrument(ed);
                }
            }
        }
    }
}
