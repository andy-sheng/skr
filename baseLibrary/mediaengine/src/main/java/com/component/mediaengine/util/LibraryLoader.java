package com.component.mediaengine.util;

import android.util.Log;

/**
 * Native library loader.
 * @hide
 */
public class LibraryLoader {
    private static final String TAG = "LibraryLoader";

    public static void load() {
        try {
            System.loadLibrary("zqlive");
        } catch (UnsatisfiedLinkError error) {
            Log.e(TAG, "No libzqlive.so! Please check");
        }
    }
}
