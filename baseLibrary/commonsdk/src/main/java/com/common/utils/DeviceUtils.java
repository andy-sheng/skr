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

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 通过U.getDeviceUtils
 */
public class DeviceUtils {
    public final static String TAG = "DeviceUtils";
    public static final String ROM_MIUI = "MIUI";
    public static final String ROM_EMUI = "EMUI";
    public static final String ROM_FLYME = "FLYME";
    public static final String ROM_OPPO = "OPPO";
    public static final String ROM_SMARTISAN = "SMARTISAN";
    public static final String ROM_VIVO = "VIVO";
    public static final String ROM_QIKU = "QIKU";

    private static final String KEY_VERSION_MIUI = "ro.miui.ui.version.name";
    private static final String KEY_VERSION_EMUI = "ro.build.version.emui";
    private static final String KEY_VERSION_OPPO = "ro.build.version.opporom";
    private static final String KEY_VERSION_SMARTISAN = "ro.smartisan.version";
    private static final String KEY_VERSION_VIVO = "ro.vivo.os.version";

    private String romName;
    private String romVersion;

    DeviceUtils() {

    }

    public boolean isEmui() {
        return check(ROM_EMUI);
    }

    public boolean isMiui() {
        return check(ROM_MIUI);
    }

    public boolean isVivo() {
        return check(ROM_VIVO);
    }

    public boolean isOppo() {
        return check(ROM_OPPO);
    }

    public boolean isFlyme() {
        return check(ROM_FLYME);
    }

    public boolean is360() {
        return check(ROM_QIKU) || check("360");
    }

    public boolean isSmartisan() {
        return check(ROM_SMARTISAN);
    }

    public boolean check(String rom) {
        if (romName != null) {
            return romName.equals(rom);
        }

        if (!TextUtils.isEmpty(romVersion = getProp(KEY_VERSION_MIUI))) {
            romName = ROM_MIUI;
        } else if (!TextUtils.isEmpty(romVersion = getProp(KEY_VERSION_EMUI))) {
            romName = ROM_EMUI;
        } else if (!TextUtils.isEmpty(romVersion = getProp(KEY_VERSION_OPPO))) {
            romName = ROM_OPPO;
        } else if (!TextUtils.isEmpty(romVersion = getProp(KEY_VERSION_VIVO))) {
            romName = ROM_VIVO;
        } else if (!TextUtils.isEmpty(romVersion = getProp(KEY_VERSION_SMARTISAN))) {
            romName = ROM_SMARTISAN;
        } else {
            romVersion = Build.DISPLAY;
            if (romVersion.toUpperCase().contains(ROM_FLYME)) {
                romName = ROM_FLYME;
            } else {
                romVersion = Build.UNKNOWN;
                romName = Build.MANUFACTURER.toUpperCase();
            }
        }
        return romName.equals(rom);
    }

    /**
     * 得到系统的属性值
     * @param name
     * @return
     */
    public String getProp(String name) {
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read prop " + name, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

}


