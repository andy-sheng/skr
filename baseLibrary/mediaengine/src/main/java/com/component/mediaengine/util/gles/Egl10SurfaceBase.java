package com.component.mediaengine.util.gles;

import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Common base class for EGL surfaces.
 * <p/>
 * There can be multiple surfaces associated with a single context.
 *
 * @hide
 */
public class Egl10SurfaceBase {
    protected static final String TAG = "Egl10SurfaceBase";

    // EglBase object we're associated with.  It may be associated with multiple surfaces.
    protected Egl10Core mEglCore;

    private EGLSurface mEGLSurface = EGL10.EGL_NO_SURFACE;
    private int mWidth = -1;
    private int mHeight = -1;

    protected Egl10SurfaceBase(Egl10Core eglBase) {
        mEglCore = eglBase;
    }

    /**
     * Creates a window surface.
     * <p/>
     *
     * @param surface May be a Surface or SurfaceTexture.
     */
    public void createWindowSurface(Object surface) {
        if (mEGLSurface != EGL10.EGL_NO_SURFACE) {
            throw new IllegalStateException("surface already created");
        }
        mEGLSurface = mEglCore.createWindowSurface(surface);
        mWidth = mEglCore.querySurface(mEGLSurface, EGL10.EGL_WIDTH);
        mHeight = mEglCore.querySurface(mEGLSurface, EGL10.EGL_HEIGHT);
    }

    /**
     * Creates an off-screen surface.
     */
    public void createOffscreenSurface(int width, int height) {
        if (mEGLSurface != EGL10.EGL_NO_SURFACE) {
            throw new IllegalStateException("surface already created");
        }
        mEGLSurface = mEglCore.createOffscreenSurface(width, height);
        mWidth = width;
        mHeight = height;
    }

    /**
     * Returns the surface's width, in pixels.
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Returns the surface's height, in pixels.
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Release the EGL surface.
     */
    public void releaseEglSurface() {
        mEglCore.releaseSurface(mEGLSurface);
        mEGLSurface = EGL10.EGL_NO_SURFACE;
        mWidth = mHeight = -1;
    }

    /**
     * Makes our EGL context and surface current.
     */
    public void makeCurrent() {
        mEglCore.makeCurrent(mEGLSurface);
    }

    /**
     * Makes our EGL context and surface current for drawing, using the supplied surface
     * for reading.
     */
    public void makeCurrentReadFrom(Egl10SurfaceBase readSurface) {
        mEglCore.makeCurrent(mEGLSurface, readSurface.mEGLSurface);
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     *
     * @return false on failure
     */
    public boolean swapBuffers() {
        boolean result = mEglCore.swapBuffers(mEGLSurface);
        if (!result) {
            Log.d(TAG, "WARNING: swapBuffers() failed");
        }
        return result;
    }
}