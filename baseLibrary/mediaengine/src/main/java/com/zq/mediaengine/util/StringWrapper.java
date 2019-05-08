package com.zq.mediaengine.util;

import java.util.List;

/**
 * @hide
 */

public class StringWrapper {
    public static int LOG_ACCESS_KEY = 0;
    public static int LOG_SECRET_KEY = 1;
    public static int COUNT_ACCESS_KEY = 2;
    public static int COUNT_SECRET_KEY = 3;

    private static int mUserCount;
    private static StringWrapper sInstance;

    public static StringWrapper getInstance() {
        synchronized (CredtpWrapper.class) {
            mUserCount++;
            if (sInstance == null) {
                synchronized (CredtpWrapper.class) {
                    if (sInstance == null) {
                        sInstance = new StringWrapper();
                    }
                }
            }
            return sInstance;
        }
    }

    public static void unInitInstance() {
        synchronized (CredtpWrapper.class) {
            mUserCount--;
            if (sInstance != null && mUserCount == 0) {
                sInstance = null;
            }
        }
    }

    public StringWrapper() {
        mStrings = getStringList();
    }

    List<String> mStrings;

    public String getStringInfo(int index) {
        if (mStrings != null) {
            return mStrings.get(index);
        } else {
            return null;
        }
    }

    private native List<String> getStringList();

    static {
        LibraryLoader.load();
    }
}
