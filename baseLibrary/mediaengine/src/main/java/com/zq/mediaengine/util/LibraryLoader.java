package com.zq.mediaengine.util;

import android.util.Log;

/**
 * Native library loader.
 * @hide
 */
public class LibraryLoader {
    private static final String TAG = "LibraryLoader";

    public static void load() {
        try {
            System.loadLibrary("unionstreamer");
        } catch (UnsatisfiedLinkError error) {
            Log.e(TAG, "No libunionstreamer.so! Please check");
        }
    }
}
