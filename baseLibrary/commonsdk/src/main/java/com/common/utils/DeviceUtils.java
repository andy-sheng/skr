package com.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    /**
     * 唯一设备号，计算方法与miui相同
     */
    private String deviceID;

    DeviceUtils() {

    }

    /**
     * 返回设备型号
     * 如 MI NOTE LTE
     *
     * @return
     */
    public String getProductModel() {
        return getProp("ro.product.model");
    }

    /**
     * 返回手机厂商
     * 如 Xiaomi
     *
     * @return
     */
    public String getProductBrand() {
        return getProp("ro.product.brand");
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
     *
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

    /**
     * 判断SDCard是否可用
     */
    public boolean existSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 检测这部手机上某个app是否已经安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public boolean isAppInstalled(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }
        return packageInfo != null;
    }


    private String getImei() {
        try {
            TelephonyManager tm = (TelephonyManager) U.app().getSystemService(Context.TELEPHONY_SERVICE);
            @SuppressLint("MissingPermission")
            String imei = tm.getDeviceId();
            return imei;
        } catch (Throwable var6) {
            return null;
        }
    }

    private String getAndroidId() {
        try {
            String androidId = Settings.Secure.getString(U.app().getContentResolver(), "android_id");
            return androidId;
        } catch (Throwable var5) {

        }
        return null;
    }

    private byte[] utf8(String var0) {
        try {
            return var0.getBytes("UTF-8");
        } catch (UnsupportedEncodingException var2) {
            return var0.getBytes();
        }
    }

    private String sha1(String str) {
        if (str != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                md.update(utf8(str));
                BigInteger r = new BigInteger(1, md.digest());
                return String.format("%1$032X", r);
            } catch (NoSuchAlgorithmException var3) {
                return str;
            }
        } else {
            return null;
        }
    }

    /**
     * 得到唯一设备ID,不只是imei
     * 就用这个作标识
     *
     * @return
     */
    public String getDeviceID() {
        if (TextUtils.isEmpty(deviceID)) {
            String imei = getImei();
            String androidId = getAndroidId();
            String serial = null;
            if (Build.VERSION.SDK_INT > 8) {
                serial = Build.SERIAL;
            }
            deviceID = sha1(imei + androidId + serial);
        }
        return deviceID;
    }
}


