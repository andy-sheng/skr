package com.component.mediaengine.util.gles;

import android.graphics.SurfaceTexture;
import android.view.Surface;

/**
 * EGL window surface.
 * <p/>
 * It's good practice to explicitly release() the surface, preferably from a "finally" block.
 * This object owns the Surface; releasing this object will release the Surface as well.
 *
 * @hide
 */
public class Egl10WindowSurface extends Egl10SurfaceBase {
    private Surface mSurface;

    /**
     * Create an offscreen EGL surface.
     */
    public Egl10WindowSurface(Egl10Core eglCore, int width, int height) {
        super(eglCore);
        createOffscreenSurface(width, height);
    }

    /**
     * Associates an EGL surface with the native window surface.  The Surface will be
     * owned by EglWindowSurface, and released when release() is called.
     */
    public Egl10WindowSurface(Egl10Core eglCore, Surface surface) {
        super(eglCore);
        createWindowSurface(surface);
        mSurface = surface;
    }

    /**
     * Associates an EGL surface with the SurfaceTexture.
     */
    public Egl10WindowSurface(Egl10Core eglCore, SurfaceTexture surfaceTexture) {
        super(eglCore);
        createWindowSurface(surfaceTexture);
    }

    /**
     * Releases any resources associated with the Surface and the EGL surface.
     */
    public void release() {
        releaseEglSurface();
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
    }

    /**
     * Recreate the EGLSurface, using the new EglBase.  The caller should have already
     * freed the old EGLSurface with releaseEglSurface().
     * <p/>
     * This is useful when we want to update the EGLSurface associated with a Surface.
     * For example, if we want to share with a different EGLContext, which can only
     * be done by tearing down and recreating the context.  (That's handled by the caller;
     * this just creates a new EGLSurface for the Surface we were handed earlier.)
     * <p/>
     * If the previous EGLSurface isn't fully destroyed, e.g. it's still current on a
     * context somewhere, the create call will fail with complaints from the Surface
     * about already being connected.
     */
    public void recreate(Egl10Core newEglCore) {
        if (mSurface == null) {
            throw new RuntimeException("not yet implemented for SurfaceTexture");
        }
        mEglCore = newEglCore;          // switch to new context
        createWindowSurface(mSurface);  // create new surface
    }
}