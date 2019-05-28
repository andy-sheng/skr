package com.common.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.common.log.MyLog;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private String romName = "";
    private String romVersion = "";

    private NotchPhoneUtils mNotchPhoneUtils;

    /**
     * 唯一设备号，计算方法与miui相同
     */
    private String deviceID;

    /**
     * 是否插着耳机，不包括蓝牙耳机
     * 1 插着
     * -1 没插
     * 0 未初始化
     */
    private int mHeadsetPlugOn = 0;

    private int mBlueToothHeadsetPlugOn = 0;

    DeviceUtils() {
        mNotchPhoneUtils = new NotchPhoneUtils();
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
        if (!TextUtils.isEmpty(romName)) {
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

    public String getRomVersion() {
        if (!TextUtils.isEmpty(romVersion)) {
            return romVersion;
        }
        check("");
        return romVersion;
    }

    /**
     * 得到系统的属性值
     *
     * @param name
     * @return
     */
    public String getProp(String name) {
        String line = "";
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read prop " + name, ex);
            return "";
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

    public String getImei() {
        try {
            TelephonyManager tm = (TelephonyManager) U.app().getSystemService(Context.TELEPHONY_SERVICE);
            @SuppressLint("MissingPermission")
            String imei = tm.getDeviceId();
            return imei;
        } catch (Throwable var6) {
            return "";
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


    private int mDeviceHasNavigationBar = -1;

//    /**
//     * 判断是否有虚拟按键
//     * 小米手机跟别的手机不一样，古怪一些
//     *
//     * @return
//     */
//    public boolean hasNavigationBar() {
//        if (true) {
//            boolean menu = ViewConfiguration.get(U.app()).hasPermanentMenuKey();
//            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
//            if (menu || back) {
//                return false;
//            } else {
//                return true;
//            }
//        }
//        if (mDeviceHasNavigationBar == -1) {
//            boolean hasNavigationBar = false;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
//                    && U.getDeviceUtils().isMiui()) {
//                hasNavigationBar = Settings.Global.getInt(U.app().getContentResolver(), "force_fsg_nav_bar", 0) == 0;
//            } else {
//                Resources rs = U.app().getResources();
//                int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
//                if (id > 0) {
//                    hasNavigationBar = rs.getBoolean(id);
//                }
//                try {
//                    Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
//                    Method m = systemPropertiesClass.getMethod("get", String.class);
//                    String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
//                    if ("1".equals(navBarOverride)) {
//                        hasNavigationBar = false;
//                    } else if ("0".equals(navBarOverride)) {
//                        hasNavigationBar = true;
//                    }
//                } catch (Exception e) {
//
//                }
//            }
//            if (hasNavigationBar) {
//                mDeviceHasNavigationBar = 1;
//            } else {
//                mDeviceHasNavigationBar = 0;
//            }
//        }
//        return mDeviceHasNavigationBar == 1;
//    }

    // 该方法需要在View完全被绘制出来之后调用，否则判断不了
    //在比如 onWindowFocusChanged（）方法中可以得到正确的结果
    public static boolean hasNavigationBar() {
        Activity activity = U.getActivityUtils().getTopActivity();
        if (activity == null) {
            return false;
        }
        ViewGroup vp = (ViewGroup) activity.getWindow().getDecorView();
        if (vp != null) {
            for (int i = 0; i < vp.getChildCount(); i++) {
                vp.getChildAt(i).getContext().getPackageName();
                if (vp.getChildAt(i).getId() != View.NO_ID && "navigationBarBackground".equals(activity.getResources().getResourceEntryName(vp.getChildAt(i).getId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getVirtualNavBarHeight() {
        if (U.getDeviceUtils().getProductModel().equals("SM-G9600")) {
            // 适配三星 G9600
            return U.getDisplayUtils().getPhoneHeight() - U.getDisplayUtils().getScreenHeight();
        }
        Resources resources = U.app().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            int naviBarHeight = resources.getDimensionPixelSize(resourceId);
            return naviBarHeight;
        } else {
            return U.getDisplayUtils().getPhoneHeight() - U.getDisplayUtils().getScreenHeight();
        }
    }

    /**
     * 耳机是否插着，包括有线 和 蓝牙
     *
     * @return
     */
    public boolean getHeadsetPlugOn() {
        return getWiredHeadsetPlugOn() || getBlueToothHeadsetOn();
    }


    public void setWiredHeadsetPlugOn(int headsetPlugOn) {
        if (headsetPlugOn != mHeadsetPlugOn) {
            mHeadsetPlugOn = headsetPlugOn;
            HeadsetPlugEvent headsetPlugEvent = new HeadsetPlugEvent(mHeadsetPlugOn == 1 || mBlueToothHeadsetPlugOn == 1);
            headsetPlugEvent.headsetPlugStatus = mHeadsetPlugOn;
            headsetPlugEvent.bluetoothStatus = mBlueToothHeadsetPlugOn;
            EventBus.getDefault().post(headsetPlugEvent);
        }
    }

    /**
     * 是否有刘海屏幕
     *
     * @return
     */
    public boolean hasNotch(Context context) {
        if (isEmui()) {
            return mNotchPhoneUtils.hasNotchInHuawei(context);
        } else if (isOppo()) {
            return mNotchPhoneUtils.hasNotchInOppo(context);
        } else if (isVivo()) {
            return mNotchPhoneUtils.hasNotchInVivo(context);
        } else if (isMiui()) {
            return mNotchPhoneUtils.hasNotchMiui(context);
        }
        return false;
    }

    /**
     * 因为一般而言，刘海的高度都会比状态栏高度低，可直接使用状态栏高度做适配
     *
     * @param context
     * @return
     */
    public int getNotchHight(Context context) {
//        if (isEmui()) {
//            return mNotchPhoneUtils.getNotchHightHuawei(context);
//        } else if (isOppo()) {
//            return mNotchPhoneUtils.getNotchHightOppo(context);
//        } else if (isVivo()) {
//            return mNotchPhoneUtils.getNotchHightVivo(context);
//        } else if (isMiui()) {
//            return mNotchPhoneUtils.getNotchHightMiui(context);
//        }
//        return 0;
        return U.getStatusBarUtil().getStatusBarHeight(context);
    }

    /**
     * 是否插着有线耳机 true 插着
     *
     * @return
     */
    public boolean getWiredHeadsetPlugOn() {
        if (mHeadsetPlugOn == 0) {
            // ==0 说明未初始化
            AudioManager audoManager = (AudioManager) U.app().getSystemService(Context.AUDIO_SERVICE);
            if (audoManager.isWiredHeadsetOn()) {
                mHeadsetPlugOn = 1;
            } else {
                mHeadsetPlugOn = -1;
            }
            // 注册广播持续监听, 这个耳机广播必须动态注册
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
            intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
            U.app().registerReceiver(new HeadsetPlugReceiver(), intentFilter);
        }
        return mHeadsetPlugOn == 1;
    }

    public void setBlueToothHeadsetPlugOn(int headsetPlugOn) {
        if (headsetPlugOn != mBlueToothHeadsetPlugOn) {
            mBlueToothHeadsetPlugOn = headsetPlugOn;
            HeadsetPlugEvent headsetPlugEvent = new HeadsetPlugEvent(mHeadsetPlugOn == 1 || mBlueToothHeadsetPlugOn == 1);
            headsetPlugEvent.headsetPlugStatus = mHeadsetPlugOn;
            headsetPlugEvent.bluetoothStatus = mBlueToothHeadsetPlugOn;
            EventBus.getDefault().post(headsetPlugEvent);
        }
    }

    /**
     * 是否插着蓝牙耳机 true 插着
     *
     * @return
     */
    public boolean getBlueToothHeadsetOn() {
        if (mBlueToothHeadsetPlugOn == 0) {
            BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
            if (ba == null) {
                mBlueToothHeadsetPlugOn = -1;
            } else if (ba.isEnabled()) {
                int a2dp = ba.getProfileConnectionState(BluetoothProfile.A2DP);              //可操控蓝牙设备，如带播放暂停功能的蓝牙耳机
                int headset = ba.getProfileConnectionState(BluetoothProfile.HEADSET);        //蓝牙头戴式耳机，支持语音输入输出
                int health = ba.getProfileConnectionState(BluetoothProfile.HEALTH);          //蓝牙穿戴式设备

                //查看是否蓝牙是否连接到三种设备的一种，以此来判断是否处于连接状态还是打开并没有连接的状态
                int flag = -1;
                if (a2dp == BluetoothProfile.STATE_CONNECTED) {
                    flag = a2dp;
                } else if (headset == BluetoothProfile.STATE_CONNECTED) {
                    flag = headset;
                } else if (health == BluetoothProfile.STATE_CONNECTED) {
                    flag = health;
                }
                //说明连接上了三种设备的一种
                if (flag != -1) {
//            isBlueCon = 1;            //connected
                    mBlueToothHeadsetPlugOn = 1;
                } else {
                    mBlueToothHeadsetPlugOn = -1;
                }
            } else {
                mBlueToothHeadsetPlugOn = -1;
            }
        }
        return mBlueToothHeadsetPlugOn == 1;
    }
    /**
     * 耳机插拔事件
     */
    public static class HeadsetPlugEvent {
        public boolean on;
        public int bluetoothStatus;
        public int headsetPlugStatus;

        public HeadsetPlugEvent(boolean on) {
            this.on = on;
        }
    }

    public static class HeadsetPlugReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                // 耳机插拔 不包括蓝牙耳机
                if (intent.hasExtra("state")) {
                    if (intent.getIntExtra("state", 0) == 0) {
                        //耳机没插
                        U.getDeviceUtils().setWiredHeadsetPlugOn(-1);
                    } else if (intent.getIntExtra("state", 0) == 1) {
                        //耳机插着
                        U.getDeviceUtils().setWiredHeadsetPlugOn(1);
                    }
                }
            } else if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter != null && BluetoothAdapter.STATE_CONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                    U.getDeviceUtils().setBlueToothHeadsetPlugOn(1);
                } else {
                    U.getDeviceUtils().setBlueToothHeadsetPlugOn(-1);
                }
            } else {
                U.getDeviceUtils().setBlueToothHeadsetPlugOn(-1);
            }
        }
    }

    /**
     * 以下为获取设备信息
     */
    private static final long MB = 1024 * 1024;
    private static final int INVALID = 0;
    private static final String MEMORY_FILE_PATH = "/proc/meminfo";
    private static final String CPU_FILE_PATH_0 = "/sys/devices/system/cpu/";
    private static final String CPU_FILE_PATH_1 = "/sys/devices/system/cpu/possible";
    private static final String CPU_FILE_PATH_2 = "/sys/devices/system/cpu/present";
    private static LEVEL sLevelCache = null;

    public static final String DEVICE_MACHINE = "machine";
    private static final String DEVICE_MEMORY_FREE = "mem_free";
    private static final String DEVICE_MEMORY = "mem";
    private static final String DEVICE_CPU = "cpu_app";

    private static long sTotalMemory = 0;
    private static long sLowMemoryThresold = 0;
    private static int sMemoryClass = 0;

    public enum LEVEL {

        BEST(5), HIGH(4), MIDDLE(3), LOW(2), BAD(1), UN_KNOW(-1);

        int value;

        LEVEL(int val) {
            this.value = val;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 得到当前设备级别，能反应出改设备的大概档次
     *
     * @return
     */
    public LEVEL getLevel() {
        if (null != sLevelCache) {
            return sLevelCache;
        }
        long start = System.currentTimeMillis();
        long totalMemory = getTotalMemory();
        int coresNum = getNumOfCores();
        if (totalMemory >= 4 * 1024 * MB) {
            sLevelCache = LEVEL.BEST;
        } else if (totalMemory >= 3 * 1024 * MB) {
            sLevelCache = LEVEL.HIGH;
        } else if (totalMemory >= 2 * 1024 * MB) {
            if (coresNum >= 4) {
                sLevelCache = LEVEL.HIGH;
            } else if (coresNum >= 2) {
                sLevelCache = LEVEL.MIDDLE;
            } else if (coresNum > 0) {
                sLevelCache = LEVEL.LOW;
            }
        } else if (totalMemory >= 1024 * MB) {
            if (coresNum >= 4) {
                sLevelCache = LEVEL.MIDDLE;
            } else if (coresNum >= 2) {
                sLevelCache = LEVEL.LOW;
            } else if (coresNum > 0) {
                sLevelCache = LEVEL.LOW;
            }
        } else if (0 <= totalMemory && totalMemory < 1024 * MB) {
            sLevelCache = LEVEL.BAD;
        } else {
            sLevelCache = LEVEL.UN_KNOW;
        }

        MyLog.i(TAG, "getLevel, cost:" + (System.currentTimeMillis() - start) + ", level:" + sLevelCache);
        return sLevelCache;
    }

    private int getAppId() {
        return android.os.Process.myPid();
    }

    /**
     * 得到低内存阈值
     *
     * @return
     */
    public long getLowMemoryThresold() {
        if (0 != sLowMemoryThresold) {
            return sLowMemoryThresold;
        }

        getTotalMemory();
        return sLowMemoryThresold;
    }

    /**
     * 为了在运行时查看可用内存，
     * <p>
     * 可用getLargeMemoryClass（）或者 getMemoryClass（）
     *
     * @return
     */
    public int getMemoryClass() {
        if (0 != sMemoryClass) {
            return sMemoryClass * 1024;
        }
        getTotalMemory();
        return sMemoryClass * 1024;
    }

    /**
     * 得到总内存
     * @return
     */
    public long getTotalMemory() {
        if (0 != sTotalMemory) {
            return sTotalMemory;
        }

        long start = System.currentTimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            ActivityManager am = (ActivityManager) U.app().getSystemService(Context.ACTIVITY_SERVICE);
            am.getMemoryInfo(memInfo);
            sTotalMemory = memInfo.totalMem;
            sLowMemoryThresold = memInfo.threshold;

            long memClass = Runtime.getRuntime().maxMemory();
            if (memClass == Long.MAX_VALUE) {
                sMemoryClass = am.getMemoryClass(); //if not set maxMemory, then is not large heap
            } else {
                sMemoryClass = (int) (memClass / MB);
            }
//            int isLargeHeap = (context.getApplicationInfo().flags | ApplicationInfo.FLAG_LARGE_HEAP);
//            if (isLargeHeap > 0) {
//                sMemoryClass = am.getLargeMemoryClass();
//            } else {
//                sMemoryClass = am.getMemoryClass();
//            }

            MyLog.i(TAG, "getTotalMemory cost:" + (System.currentTimeMillis() - start) + ", total_mem:" + sTotalMemory
                    + ", LowMemoryThresold:" + sLowMemoryThresold + ", Memory Class:" + sMemoryClass);
            return sTotalMemory;
        }
        return 0;
    }

    /**
     * 是否是低内存
     *
     * @return
     */
    public boolean isLowMemory() {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) U.app().getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(memInfo);
        return memInfo.lowMemory;
    }

    //return in KB
    public long getAvailMemory(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            am.getMemoryInfo(memInfo);
            return memInfo.availMem / 1024;
        } else {
            long availMemory = INVALID;
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(MEMORY_FILE_PATH), "UTF-8"));
                String line = bufferedReader.readLine();
                while (null != line) {
                    String[] args = line.split("\\s+");
                    if ("MemFree:".equals(args[0])) {
                        availMemory = Integer.parseInt(args[1]) * 1024L;
                        break;
                    } else {
                        line = bufferedReader.readLine();
                    }
                }

            } catch (Exception e) {
                MyLog.e(TAG, e);
            } finally {
                try {
                    if (null != bufferedReader) {
                        bufferedReader.close();
                    }
                } catch (Exception e) {
                    MyLog.e(TAG, e);
                }
            }
            return availMemory / 1024;
        }
    }

    public  long getMemFree() {
        long availMemory = INVALID;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(MEMORY_FILE_PATH), "UTF-8"));
            String line = bufferedReader.readLine();
            while (null != line) {
                String[] args = line.split("\\s+");
                if ("MemFree:".equals(args[0])) {
                    availMemory = Integer.parseInt(args[1]) * 1024L;
                    break;
                } else {
                    line = bufferedReader.readLine();
                }
            }

        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            try {
                if (null != bufferedReader) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }
        return availMemory / 1024;
    }

    public double getAppCpuRate() {
        long start = System.currentTimeMillis();
        long cpuTime = 0L;
        long appTime = 0L;
        double cpuRate = 0.0D;
        RandomAccessFile procStatFile = null;
        RandomAccessFile appStatFile = null;

        try {
            procStatFile = new RandomAccessFile("/proc/stat", "r");
            String procStatString = procStatFile.readLine();
            String[] procStats = procStatString.split(" ");
            cpuTime = Long.parseLong(procStats[2]) + Long.parseLong(procStats[3])
                    + Long.parseLong(procStats[4]) + Long.parseLong(procStats[5])
                    + Long.parseLong(procStats[6]) + Long.parseLong(procStats[7])
                    + Long.parseLong(procStats[8]);

        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            try {
                if (null != procStatFile) {
                    procStatFile.close();
                }

            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }

        try {
            appStatFile = new RandomAccessFile("/proc/" + getAppId() + "/stat", "r");
            String appStatString = appStatFile.readLine();
            String[] appStats = appStatString.split(" ");
            appTime = Long.parseLong(appStats[13]) + Long.parseLong(appStats[14]);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            try {
                if (null != appStatFile) {
                    appStatFile.close();
                }
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }

        if (0 != cpuTime) {
            cpuRate = ((double) (appTime) / (double) (cpuTime)) * 100D;
        }

        MyLog.i(TAG, "getAppCpuRate cost:" + (System.currentTimeMillis() - start) + ",rate:" + cpuRate);
        return cpuRate;
    }

    public Debug.MemoryInfo getAppMemory(Context context) {
        try {
            // 统计进程的内存信息 totalPss
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            final Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(new int[]{getAppId()});
            if (memInfo.length > 0) {
                return memInfo[0];
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
        return null;
    }

    private int getNumOfCores() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            return 1;
        }
        int cores;
        try {
            cores = getCoresFromFile(CPU_FILE_PATH_1);
            if (cores == INVALID) {
                cores = getCoresFromFile(CPU_FILE_PATH_2);
            }
            if (cores == INVALID) {
                cores = getCoresFromCPUFiles(CPU_FILE_PATH_0);
            }
        } catch (Exception e) {
            cores = INVALID;
        }
        if (cores == INVALID) {
            cores = 1;
        }
        return cores;
    }

    private int getCoresFromCPUFiles(String path) {
        File[] list = new File(path).listFiles(CPU_FILTER);
        return null == list ? 0 : list.length;
    }

    private int getCoresFromFile(String file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String fileContents = buf.readLine();
            buf.close();
            if (fileContents == null || !fileContents.matches("0-[\\d]+$")) {
                return INVALID;
            }
            String num = fileContents.substring(2);
            return Integer.parseInt(num) + 1;
        } catch (IOException e) {
            return INVALID;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
        }
    }

    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return Pattern.matches("cpu[0-9]", pathname.getName());
        }
    };

    public long getDalvikHeap() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;   //in KB
    }

    public long getNativeHeap() {
        return Debug.getNativeHeapAllocatedSize() / 1024;   //in KB
    }

    public long getVmSize() {
        String status = String.format("/proc/%s/status", getAppId());
        try {
            String content = getStringFromFile(status).trim();
            String[] args = content.split("\n");
            for (String str : args) {
                if (str.startsWith("VmSize")) {
                    Pattern p = Pattern.compile("\\d+");
                    Matcher matcher = p.matcher(str);
                    if (matcher.find()) {
                        return Long.parseLong(matcher.group());
                    }
                }
            }
            if (args.length > 12) {
                Pattern p = Pattern.compile("\\d+");
                Matcher matcher = p.matcher(args[12]);
                if (matcher.find()) {
                    return Long.parseLong(matcher.group());
                }
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    protected  String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            if (null != reader) {
                reader.close();
            }
        }

        return sb.toString();
    }

    protected  String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = null;
        String ret;
        try {
            fin = new FileInputStream(fl);
            ret = convertStreamToString(fin);
        } finally {
            if (null != fin) {
                fin.close();
            }
        }
        return ret;
    }

}



