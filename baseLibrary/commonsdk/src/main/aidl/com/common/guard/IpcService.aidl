// IpcService.aidl
package com.common.guard;
import com.common.guard.IpcCallback;

interface IpcService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void call(int type, String json, IpcCallback callback);
}
