// WebIpcService.aidl
package com.common.core;

// Declare any non-default types here with import statements
import com.common.core.WebIpcCallback;

interface WebIpcService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void call(int type, String json, WebIpcCallback callback);
}
