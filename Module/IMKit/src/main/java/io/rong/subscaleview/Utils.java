//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.subscaleview;

import android.content.Context;
import android.opengl.GLES10;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class Utils {
  public Utils() {
  }

  public static int getMaxLoader() {
    return VERSION.SDK_INT >= 21 ? getGLESTextureLimitEqualAboveLollipop() : getGLESTextureLimitBelowLollipop();
  }

  private static int getGLESTextureLimitBelowLollipop() {
    int[] maxSize = new int[1];
    GLES10.glGetIntegerv(3379, maxSize, 0);
    return maxSize[0];
  }

  private static int getGLESTextureLimitEqualAboveLollipop() {
    EGL10 egl = (EGL10)EGLContext.getEGL();
    EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
    int[] vers = new int[2];
    egl.eglInitialize(dpy, vers);
    int[] configAttr = new int[]{12351, 12430, 12329, 0, 12339, 1, 12344};
    EGLConfig[] configs = new EGLConfig[1];
    int[] numConfig = new int[1];
    egl.eglChooseConfig(dpy, configAttr, configs, 1, numConfig);
    if (numConfig[0] == 0) {
      ;
    }

    EGLConfig config = configs[0];
    int[] surfAttr = new int[]{12375, 64, 12374, 64, 12344};
    EGLSurface surf = egl.eglCreatePbufferSurface(dpy, config, surfAttr);
    int[] ctxAttrib = new int[]{12440, 1, 12344};
    EGLContext ctx = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, ctxAttrib);
    egl.eglMakeCurrent(dpy, surf, surf, ctx);
    int[] maxSize = new int[1];
    GLES10.glGetIntegerv(3379, maxSize, 0);
    egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
    egl.eglDestroySurface(dpy, surf);
    egl.eglDestroyContext(dpy, ctx);
    egl.eglTerminate(dpy);
    return maxSize[0];
  }

  public static int getScreenWidth(Context context) {
    DisplayMetrics metrics = context.getApplicationContext().getResources().getDisplayMetrics();
    return metrics.widthPixels;
  }

  public static int getScreenHeight(Context context) {
    DisplayMetrics metrics = context.getApplicationContext().getResources().getDisplayMetrics();
    return metrics.heightPixels;
  }
}
