/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.common.log.MyLog;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 通过U.getNetworkUtils 主动获取当前的网络状态
 * 如果想实时监听网络状态，请用 Eventbus 监听 {@link NetworkChangeEvent }
 */
public class NetworkUtils {
    public final static String TAG = "NetworkUtils";

    NetworkUtils() {
    }

    int type = -1;
    int subType = -1;

    public boolean is2G() {
        syncActiveNetworkType();
        if (isMobile()) {
            if (subType == TelephonyManager.NETWORK_TYPE_GPRS
                    || subType == TelephonyManager.NETWORK_TYPE_CDMA
                    || subType == TelephonyManager.NETWORK_TYPE_EDGE
                    || subType == TelephonyManager.NETWORK_TYPE_1xRTT
                    || subType == TelephonyManager.NETWORK_TYPE_IDEN) {
                return true;
            }
        }
        return false;
    }

    public boolean is3G() {
        syncActiveNetworkType();
        if (isMobile()) {
            if (subType == TelephonyManager.NETWORK_TYPE_EVDO_A
                    || subType == TelephonyManager.NETWORK_TYPE_UMTS
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    || subType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || subType == TelephonyManager.NETWORK_TYPE_HSUPA
                    || subType == TelephonyManager.NETWORK_TYPE_HSPA
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_B
                    || subType == TelephonyManager.NETWORK_TYPE_EHRPD
                    || subType == TelephonyManager.NETWORK_TYPE_HSPAP
                    ) {
                return true;
            }
        }
        return false;
    }

    public boolean is4G() {
        syncActiveNetworkType();
        if (isMobile()) {
            if (subType == TelephonyManager.NETWORK_TYPE_LTE
                    || subType == 19) {
                return true;
            }
        }
        return false;
    }

    public boolean isMobile() {
        syncActiveNetworkType();
        return type == ConnectivityManager.TYPE_MOBILE;
    }

    public boolean isWifi() {
        syncActiveNetworkType();
        return type == ConnectivityManager.TYPE_WIFI;
    }

    public boolean hasNetwork() {
        syncActiveNetworkType();
        return type != -1;
    }

    void syncActiveNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) U.app().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null) {
                type = info.getType();
                subType = info.getSubtype();
            }else{
                type=-1;
                subType=-1;
            }
        }
    }

    void notifyNetworkChange() {
        int oldType = type;
        int oldSubtype = subType;
        syncActiveNetworkType();
        MyLog.d(TAG, "notifyNetworkChange oldType:" + oldType + " type:" + type);

        if (type != oldType) {
            EventBus.getDefault().post(new NetworkChangeEvent(oldType, type));
        }
    }

    /**
     * 可以监听这个事件得到网络变化
     */
    public static class NetworkChangeEvent {
        public int oldType;
        public int type; // -1 代表没网

        public NetworkChangeEvent(int oldType, int type) {
            this.oldType = oldType;
            this.type = type;
        }
    }
}


