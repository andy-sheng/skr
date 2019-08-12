package com.zq.mediaengine.util.gles;

import android.opengl.GLES20;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * fbo cache manager
 *
 * @hide
 */
public class FboManager {
    private static final String TAG = "FboManager";
    private static boolean VERBOSE = true;
    private static boolean TRACE_FBO_REUSE = false;

    private HashMap<String, List<Integer>> mResolutionMap;
    private HashMap<Integer, Fbo> mTextureMap;
    private int mTextureCount;

    public FboManager() {
        mResolutionMap = new HashMap<>();
        mTextureMap = new HashMap<>();
    }

    synchronized public void init() {
        if (VERBOSE) Log.d(TAG, "init");
        mTextureMap.clear();
        mResolutionMap.clear();
        mTextureCount = 0;
    }

    /**
     * Generate or reuse a fbo with given width and height, in GL_RGBA format, then lock it.
     *
     * @param w fbo width
     * @param h fbo height
     * @return texture id or -1 on error
     */
    synchronized public int getTextureAndLock(int w, int h) {
        Fbo fbo;
        String key = genKey(w, h);
        List<Integer> texList = mResolutionMap.get(key);
        if (texList == null) {
            texList = new LinkedList<>();
            mResolutionMap.put(key, texList);
        }
        // reuse unlocked fbo
        for (int texture : texList) {
            fbo = mTextureMap.get(texture);
            if (!fbo.isLocked()) {
                fbo.lock();
                if (TRACE_FBO_REUSE) {
                    Log.d(TAG, "reuse and lock " + texture);
                }
                return texture;
            }
        }
        // create a new fbo
        fbo = createFbo(w, h);
        mTextureCount++;
        if (VERBOSE) {
            Log.d(TAG, "Create and lock a new fbo: " + fbo.texture + " " +
                    w + "x" + h + " total:" + mTextureCount);
        }
        fbo.lock();
        mTextureMap.put(fbo.texture, fbo);
        texList.add(fbo.texture);
        return fbo.texture;
    }

    /**
     * Get framebuffer from bind texture.
     *
     * @param texture texture id
     * @return framebuffer bind with given texture or -1
     */
    synchronized public int getFramebuffer(int texture) {
        Fbo fbo = mTextureMap.get(texture);
        if (fbo != null) {
            return fbo.framebuffer;
        }
        return -1;
    }

    /**
     * Increase the fbo reference count.
     *
     * @param texture texture id to be locked
     * @return true on success, false otherwise
     */
    synchronized public boolean lock(int texture) {
        return lock(texture, 1);
    }

    /**
     * Increase the fbo reference count with given count.
     *
     * @param texture texture id to be locked
     * @param count   reference count to be added
     * @return true on success, false otherwise
     */
    synchronized public boolean lock(int texture, int count) {
        Fbo fbo = mTextureMap.get(texture);
        if (TRACE_FBO_REUSE && fbo != null) {
            Log.d(TAG, "lock: " + texture + " " + count + " times");
        }
        if (fbo == null) {
            return false;
        }
        fbo.lock(count);
        return true;
    }

    /**
     * Decrease the fbo reference count, so it can be reused by other filters.
     *
     * @param texture texture id to be unlocked
     * @return true on success, false otherwise
     */
    synchronized public boolean unlock(int texture) {
        Fbo fbo = mTextureMap.get(texture);
        if (TRACE_FBO_REUSE && fbo != null) {
            Log.d(TAG, "unlock: " + texture);
        }
        return fbo != null && fbo.unlock();
    }

    /**
     * Get current total fbo count
     *
     * @return current generate fbo count
     */
    synchronized public int getTextureCount() {
        return mTextureCount;
    }

    /**
     * Remove the given texture and the fbo bind on it
     *
     * @param texture texture id
     */
    synchronized public void remove(int texture) {
        Fbo fbo = mTextureMap.get(texture);
        if (fbo == null) {
            return;
        }

        mTextureCount--;
        mTextureMap.remove(texture);
        String key = genKey(fbo.width, fbo.height);
        List<Integer> texList = mResolutionMap.get(key);
        texList.remove(Integer.valueOf(texture));
        releaseFbo(fbo);
    }

    /**
     * Remove all textures and the fbo bind on them
     */
    synchronized public void remove() {
        if (VERBOSE) Log.d(TAG, "remove all " + mTextureMap.size() + " fbo");

        int[] textures = new int[mTextureMap.size()];
        int[] buffers = new int[mTextureMap.size()];
        int i = 0;
        for (Fbo fbo : mTextureMap.values()) {
            textures[i] = fbo.texture;
            buffers[i] = fbo.framebuffer;
            i++;
        }
        GLES20.glDeleteTextures(textures.length, textures, 0);
        GLES20.glDeleteFramebuffers(buffers.length, buffers, 0);

        mTextureMap.clear();
        mResolutionMap.clear();
        mTextureCount = 0;
    }

    private String genKey(int w, int h) {
        return Integer.toString(w) + "x" + Integer.toString(h);
    }

    private Fbo createFbo(int width, int height) {
        int[] buffers = new int[1];
        int[] textures = new int[1];

        GLES20.glGenFramebuffers(1, buffers, 0);
        GLES20.glGenTextures(1, textures, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, buffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textures[0], 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return new Fbo(width, height, buffers[0], textures[0]);
    }

    private void releaseFbo(Fbo fbo) {
        int[] textures = {fbo.texture};
        int[] buffers = {fbo.framebuffer};
        GLES20.glDeleteTextures(1, textures, 0);
        GLES20.glDeleteFramebuffers(1, buffers, 0);
    }

    private class Fbo {
        public final int width;
        public final int height;
        public final int framebuffer;
        public final int texture;

        private int count;

        public Fbo(int width, int height, int framebuffer, int texture) {
            this.width = width;
            this.height = height;
            this.framebuffer = framebuffer;
            this.texture = texture;
            count = 0;
        }

        synchronized public boolean isLocked() {
            return (count != 0);
        }

        synchronized public void lock() {
            count++;
        }

        synchronized public void lock(int count) {
            this.count += count;
        }

        synchronized public boolean unlock() {
            if (count == 0) {
                return false;
            }
            count--;
            if (TRACE_FBO_REUSE && count == 0) {
                Log.d(TAG, "fbo " + texture + " released");
            }
            return true;
        }
    }
}
