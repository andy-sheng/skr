package com.zq.mediaengine.util.gles;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL render thread manager.
 * Support GLSurfaceView and TextureView.
 */
public class GLRender {
    private static final String TAG = "GLRender";
    private static final boolean VERBOSE = true;

    public static final int STATE_IDLE = 0;
    public static final int STATE_READY = 1;
    public static final int STATE_RELEASED = 2;

    public static final int VIEW_TYPE_NONE = 0;
    public static final int VIEW_TYPE_GLSURFACEVIEW = 1;
    public static final int VIEW_TYPE_TEXTUREVIEW = 2;
    public static final int VIEW_TYPE_OFFSCREEN = 3;

    private static final int CMD_READY = 0;
    private static final int CMD_SIZE_CHANGED = 1;
    private static final int CMD_DRAW_FRAME = 2;
    private static final int CMD_RELEASE = 3;

    private TextureView mTextureView;
    private final Object mGLThreadLock = new Object();
    private HandlerThread mGLThread;
    private Handler mGLHandler;
    private Egl10Core mEglCore;
    private Egl10WindowSurface mWindowSurface;
    private EGLContext mInitEGL10Context;
    private EGLContext mEGL10Context;
    private android.opengl.EGLContext mEGLContext;

    private AtomicInteger mState;
    private long mGLThreadId;
    private GLSurfaceView mGLSurfaceView;
    private final LinkedList<GLRenderListener> mListeners;
    private final LinkedList<OnReadyListener> mReadyListeners;
    private final LinkedList<OnSizeChangedListener> mSizeChangedListeners;
    private final LinkedList<OnDrawFrameListener> mDrawFrameListeners;
    private final LinkedList<OnReleasedListener> mReleasedListeners;
    private final LinkedList<Runnable> mEvents;
    private final LinkedList<Runnable> mDrawFrameAppends;

    private FboManager mFboManager;

    /**
     * GLRender listener interface.
     *
     * @deprecated use separated {@link OnReadyListener} {@link OnSizeChangedListener}
     * {@link OnDrawFrameListener} {@link OnReleasedListener} instead.
     */
    @Deprecated
    public interface GLRenderListener {

        /**
         * GLContext ready
         */
        void onReady();

        /**
         * GLRenderer size changed
         *
         * @param width  width
         * @param height height
         */
        void onSizeChanged(int width, int height);

        /**
         * Draw frame on GL thread
         */
        void onDrawFrame();

        /**
         * GLContext released
         */
        void onReleased();
    }

    public interface OnReadyListener {
        /**
         * GLContext ready
         */
        void onReady();
    }

    public interface OnSizeChangedListener {
        /**
         * GLRenderer size changed
         *
         * @param width  width
         * @param height height
         */
        void onSizeChanged(int width, int height);
    }

    public interface OnDrawFrameListener {
        /**
         * Draw frame on GL thread
         */
        void onDrawFrame();
    }

    public interface OnReleasedListener {
        /**
         * GLContext released
         */
        void onReleased();
    }

    public interface ScreenShotListener {
        void onBitmapAvailable(Bitmap bitmap);
    }

    public GLRender() {
        mState = new AtomicInteger(STATE_RELEASED);
        mListeners = new LinkedList<>();
        mReadyListeners = new LinkedList<>();
        mSizeChangedListeners = new LinkedList<>();
        mDrawFrameListeners = new LinkedList<>();
        mReleasedListeners = new LinkedList<>();
        mEvents = new LinkedList<>();
        mDrawFrameAppends = new LinkedList<>();
        mInitEGL10Context = EGL10.EGL_NO_CONTEXT;
        mEGL10Context = EGL10.EGL_NO_CONTEXT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mEGLContext = EGL14.EGL_NO_CONTEXT;
        }
        mFboManager = new FboManager();
    }

    public GLRender(EGLContext eglContext) {
        this();
        mInitEGL10Context = eglContext;
    }

    public void setInitEGL10Context(EGLContext eglContext) {
        mInitEGL10Context = eglContext;
    }

    public android.opengl.EGLContext getEGLContext() {
        return mEGLContext;
    }

    public EGLContext getEGL10Context() {
        return mEGL10Context;
    }

    /**
     * Init GLRender manager with offscreen surface.
     *
     * @param width  offscreen width
     * @param height offscreen height
     */
    public void init(int width, int height) {
        release();
        mState.set(STATE_IDLE);
        initGLThread();
        Message msg = Message.obtain(mGLHandler, CMD_READY, width, height);
        mGLHandler.sendMessage(msg);
        msg = Message.obtain(mGLHandler, CMD_SIZE_CHANGED, width, height);
        mGLHandler.sendMessage(msg);
    }

    /**
     * Init GLRender manager, must be called before GLSurfaceView created.
     *
     * @param glSurfaceView dedicated GLSurfaceView
     */
    public void init(GLSurfaceView glSurfaceView) {
        if (glSurfaceView == mGLSurfaceView) {
            return;
        }
        release();
        mState.set(STATE_IDLE);
        try {
            glSurfaceView.setEGLConfigChooser(mEGLConfigChooser);
            glSurfaceView.setEGLContextFactory(mEGLContextFactory);
            glSurfaceView.setEGLContextClientVersion(2);
            glSurfaceView.setRenderer(mSurfaceViewRenderer);
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        } catch (Exception e) {
            // do nothing
        }
        mGLSurfaceView = glSurfaceView;
    }

    /**
     * Init GLRender manager, must be called before TextureView ready.
     *
     * @param textureView dedicated TextureView
     */
    public void init(TextureView textureView) {
        if (textureView == mTextureView) {
            return;
        }
        release();
        mState.set(STATE_IDLE);
        textureView.setSurfaceTextureListener(mSurfaceTextureListener);
        mTextureView = textureView;
        if (textureView.getSurfaceTexture() != null) {
            Log.d(TAG, "TextureView already initialized");
            initGLThread();
            Message msg = Message.obtain(mGLHandler, CMD_READY, textureView.getSurfaceTexture());
            mGLHandler.sendMessage(msg);
            msg = Message.obtain(mGLHandler, CMD_SIZE_CHANGED, textureView.getWidth(),
                    textureView.getHeight());
            mGLHandler.sendMessage(msg);
        }
    }

    public int getViewType() {
        if (mGLSurfaceView == null && mTextureView == null && mGLThread == null) {
            return VIEW_TYPE_NONE;
        } else if (mGLSurfaceView != null) {
            return VIEW_TYPE_GLSURFACEVIEW;
        } else if (mTextureView != null) {
            return VIEW_TYPE_TEXTUREVIEW;
        } else {
            return VIEW_TYPE_OFFSCREEN;
        }
    }

    public View getCurrentView() {
        if (mGLSurfaceView != null) {
            return mGLSurfaceView;
        } else if (mTextureView != null) {
            return mTextureView;
        } else {
            return null;
        }
    }

    /**
     * Add GLRenderListener to current GLRender.
     *
     * Should be removed by calling {@link #removeListener(GLRenderListener)}.
     *
     * @param listener listener to be added
     * @deprecated Use separated {@link #addListener(OnReadyListener)}
     * {@link #addListener(OnSizeChangedListener)} {@link #addListener(OnDrawFrameListener)}
     * {@link #addListener(OnReleasedListener)} instead.
     */
    @Deprecated
    public void addListener(final GLRenderListener listener) {
        synchronized (mListeners) {
            if (!mListeners.contains(listener)) {
                if (mState.get() == STATE_READY) {
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            listener.onReady();
                        }
                    });
                }
                mListeners.add(listener);
            }
        }
    }

    /**
     * Remove GLRenderListener.
     *
     * @param listener listener to be removed.
     * @deprecated Use separated {@link #removeListener(OnReadyListener)}
     * {@link #removeListener(OnSizeChangedListener)} {@link #removeListener(OnDrawFrameListener)}
     * {@link #removeListener(OnReleasedListener)} instead.
     */
    @Deprecated
    public void removeListener(GLRenderListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    public void addListener(final OnReadyListener listener) {
        synchronized (mReadyListeners) {
            if (!mReadyListeners.contains(listener)) {
                if (mState.get() == STATE_READY) {
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            listener.onReady();
                        }
                    });
                }
                mReadyListeners.add(listener);
            }
        }
    }

    public void removeListener(OnReadyListener listener) {
        synchronized (mReadyListeners) {
            mReadyListeners.remove(listener);
        }
    }

    public void addListener(OnSizeChangedListener listener) {
        synchronized (mSizeChangedListeners) {
            if (!mSizeChangedListeners.contains(listener)) {
                mSizeChangedListeners.add(listener);
            }
        }
    }

    public void removeListener(OnSizeChangedListener listener) {
        synchronized (mSizeChangedListeners) {
            mSizeChangedListeners.remove(listener);
        }
    }

    public void addListener(OnDrawFrameListener listener) {
        synchronized (mDrawFrameListeners) {
            if (!mDrawFrameListeners.contains(listener)) {
                mDrawFrameListeners.add(listener);
            }
        }
    }

    public void removeListener(OnDrawFrameListener listener) {
        synchronized (mDrawFrameListeners) {
            mDrawFrameListeners.remove(listener);
        }
    }

    public void addListener(OnReleasedListener listener) {
        synchronized (mReleasedListeners) {
            if (!mReleasedListeners.contains(listener)) {
                mReleasedListeners.add(listener);
            }
        }
    }

    public void removeListener(OnReleasedListener listener) {
        synchronized (mReleasedListeners) {
            mReleasedListeners.remove(listener);
        }
    }

    public int getState() {
        return mState.get();
    }

    public boolean isGLRenderThread() {
        return mGLThreadId == Thread.currentThread().getId();
    }

    public void onPause() {
        if (mGLSurfaceView != null) {
            mState.set(STATE_RELEASED);
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    GLRender.this.onReleased();
                }
            });
            mGLSurfaceView.onPause();
        }
    }

    public void onResume() {
        if (mState.get() == STATE_RELEASED) {
            mState.set(STATE_IDLE);
        }
        if (mGLSurfaceView != null) {
            mGLSurfaceView.onResume();
        }
    }

    public void requestRender() {
        if (mState.get() != STATE_READY) {
            return;
        }
        if (mGLSurfaceView != null) {
            mGLSurfaceView.requestRender();
        }
        if (mGLHandler != null) {
            mGLHandler.sendEmptyMessage(CMD_DRAW_FRAME);
        }
    }

    public void queueEvent(Runnable r) {
        if (mState.get() == STATE_IDLE) {
            if (VERBOSE) Log.d(TAG, "glContext not ready, queue event:" + r);
            synchronized (mEvents) {
                mEvents.add(r);
            }
        } else if (mState.get() == STATE_READY) {
            if (mGLSurfaceView != null) {
                mGLSurfaceView.queueEvent(r);
                mGLSurfaceView.queueEvent(mDrainDrawFrameAppends);
            } else if (mGLHandler != null) {
                mGLHandler.post(r);
                mGLHandler.post(mDrainDrawFrameAppends);
            }
        } else {
            if (VERBOSE) Log.d(TAG, "glContext lost, drop event:" + r);
        }
    }

    public void queueDrawFrameAppends(Runnable r) {
        if (mState.get() == STATE_READY) {
            synchronized (mDrawFrameAppends) {
                mDrawFrameAppends.add(r);
            }
        }
    }

    public void release() {
        if (mGLSurfaceView != null && mState.get() == STATE_READY) {
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    GLRender.this.onReleased();
                }
            });
            mGLSurfaceView.onPause();
        }
        mGLSurfaceView = null;
        mTextureView = null;
        mState.set(STATE_RELEASED);
        releaseGLThread(null);
    }

    public FboManager getFboManager() {
        return mFboManager;
    }

    private void onReady() {
        mGLThreadId = Thread.currentThread().getId();
        mFboManager.init();
        GLES20.glEnable(GL10.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        mEGL10Context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mEGLContext = EGL14.eglGetCurrentContext();
        }
        mState.set(STATE_READY);

        // execute queued events
        synchronized (mEvents) {
            for (Runnable r : mEvents) {
                queueEvent(r);
            }
            mEvents.clear();
        }

        synchronized (mListeners) {
            for (GLRenderListener listener : mListeners) {
                listener.onReady();
            }
        }
        synchronized (mReadyListeners) {
            for (OnReadyListener listener : mReadyListeners) {
                listener.onReady();
            }
        }
    }

    private void onSizeChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        synchronized (mListeners) {
            for (GLRenderListener listener : mListeners) {
                listener.onSizeChanged(width, height);
            }
        }
        synchronized (mSizeChangedListeners) {
            for (OnSizeChangedListener listener : mSizeChangedListeners) {
                listener.onSizeChanged(width, height);
            }
        }
    }

    private void onDrawFrame() {
        // execute queued events
        synchronized (mEvents) {
            for (Runnable r : mEvents) {
                r.run();
            }
            mEvents.clear();
        }

        synchronized (mListeners) {
            for (GLRenderListener listener : mListeners) {
                listener.onDrawFrame();
            }
        }
        synchronized (mDrawFrameListeners) {
            for (OnDrawFrameListener listener : mDrawFrameListeners) {
                listener.onDrawFrame();
            }
        }

        // transfer recursive call to loop
        drainDrawFrameAppends();
    }

    private void onReleased() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mEGLContext = EGL14.EGL_NO_CONTEXT;
        }
        mState.set(STATE_RELEASED);
        synchronized (mListeners) {
            for (GLRenderListener listener : mListeners) {
                listener.onReleased();
            }
        }
        synchronized (mReleasedListeners) {
            for (OnReleasedListener listener : mReleasedListeners) {
                listener.onReleased();
            }
        }
    }

    private void drainDrawFrameAppends() {
        while (true) {
            Runnable r;
            synchronized (mDrawFrameAppends) {
                if (mDrawFrameAppends.isEmpty()) {
                    break;
                } else {
                    r = mDrawFrameAppends.getFirst();
                    mDrawFrameAppends.removeFirst();
                }
            }
            r.run();
        }
    }

    private Runnable mDrainDrawFrameAppends = new Runnable() {
        @Override
        public void run() {
            drainDrawFrameAppends();
        }
    };

    private GLSurfaceView.EGLContextFactory mEGLContextFactory =
            new GLSurfaceView.EGLContextFactory() {
        @Override
        public EGLContext createContext(
                EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
            int[] ctxAttrib = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
            };
            return egl.eglCreateContext(display, eglConfig, mInitEGL10Context, ctxAttrib);
        }

        @Override
        public void destroyContext(EGL10 egl, EGLDisplay display,
                                   EGLContext context) {
            egl.eglDestroyContext(display, context);
            mEGL10Context = EGL10.EGL_NO_CONTEXT;
        }
    };

    private GLSurfaceView.EGLConfigChooser mEGLConfigChooser =
            new GLSurfaceView.EGLConfigChooser() {
        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            final int EGL_OPENGL_ES2_BIT = 4;
            int[] attribList = {
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, 8,
                    //EGL10.EGL_DEPTH_SIZE, 16,
                    //EGL10.EGL_STENCIL_SIZE, 8,
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                    EGL10.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!egl.eglChooseConfig(display, attribList, configs, configs.length, numConfigs)) {
                Log.w(TAG, "unable to find RGB8888 / 2 EGLConfig");
                return null;
            }
            return configs[0];
        }
    };

    private GLSurfaceView.Renderer mSurfaceViewRenderer = new GLSurfaceView.Renderer() {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            onReady();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            onSizeChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLRender.this.onDrawFrame();
        }
    };

    private void initGLContext(SurfaceTexture surfaceTexture, int width, int height) {
        mEglCore = new Egl10Core(mInitEGL10Context, 0);
        if (surfaceTexture != null) {
            mWindowSurface = new Egl10WindowSurface(mEglCore, surfaceTexture);
        } else {
            mWindowSurface = new Egl10WindowSurface(mEglCore, width, height);
        }
        mWindowSurface.makeCurrent();
        GLES20.glViewport(0, 0, mWindowSurface.getWidth(), mWindowSurface.getHeight());
    }

    private void releaseGLContext(SurfaceTexture surfaceTexture) {
        if (surfaceTexture != null) {
            surfaceTexture.release();
        }
        if (mWindowSurface != null) {
            mWindowSurface.release();
            mWindowSurface = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    private void initGLThread() {
        synchronized (mGLThreadLock) {
            if (mGLThread == null) {
                mGLThread = new HandlerThread("GLThread");
                mGLThread.start();
                mGLHandler = new Handler(mGLThread.getLooper(), new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        switch (msg.what) {
                            case CMD_READY:
                                initGLContext((SurfaceTexture) msg.obj, msg.arg1, msg.arg2);
                                onReady();
                                break;
                            case CMD_SIZE_CHANGED:
                                onSizeChanged(msg.arg1, msg.arg2);
                                break;
                            case CMD_DRAW_FRAME:
                                onDrawFrame();
                                mWindowSurface.swapBuffers();
                                break;
                            case CMD_RELEASE:
                                onReleased();
                                releaseGLContext((SurfaceTexture) msg.obj);
                                mGLThread.quit();
                                break;
                        }
                        return true;
                    }
                });
            }
        }
    }

    private void releaseGLThread(SurfaceTexture surfaceTexture) {
        synchronized (mGLThreadLock) {
            if (mGLThread != null) {
                mGLHandler.removeCallbacksAndMessages(null);
                Message msg = Message.obtain(mGLHandler, CMD_RELEASE, surfaceTexture);
                mGLHandler.sendMessage(msg);
                try {
                    mGLThread.join();
                } catch (InterruptedException e) {
                    Log.d(TAG, "GLThread Interrupted!");
                } finally {
                    mGLThread = null;
                    mGLHandler = null;
                }
            } else if (surfaceTexture != null) {
                surfaceTexture.release();
            }
        }
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    if (VERBOSE) {
                        Log.d(TAG, "onSurfaceTextureAvailable " + width + "x" + height);
                    }
                    if (mTextureView == null || mGLThread != null) {
                        return;
                    }
                    initGLThread();
                    Message msg = Message.obtain(mGLHandler, CMD_READY, surface);
                    mGLHandler.sendMessage(msg);
                    msg = Message.obtain(mGLHandler, CMD_SIZE_CHANGED, width, height);
                    mGLHandler.sendMessage(msg);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    if (VERBOSE) {
                        Log.d(TAG, "onSurfaceTextureSizeChanged " + width + "x" + height);
                    }
                    if (mTextureView == null || surface != mTextureView.getSurfaceTexture()) {
                        return;
                    }
                    Message msg = Message.obtain(mGLHandler, CMD_SIZE_CHANGED, width, height);
                    mGLHandler.sendMessage(msg);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    if (VERBOSE) {
                        Log.d(TAG, "onSurfaceTextureDestroyed");
                    }
                    if (mTextureView != null && surface == mTextureView.getSurfaceTexture()) {
                        // release surfaceTexture in GLThread.
                        releaseGLThread(surface);
                    } else {
                        surface.release();
                    }
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                    // need do nothing.
                }
            };
}
