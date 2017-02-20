package com.base.utils.version;

import android.content.Context;
import android.os.Build;

import com.base.global.GlobalData;
import com.base.preference.PreferenceUtils;
import com.base.utils.Constants;
import com.base.utils.channel.ReleaseChannelUtils;

/**
 * Created by lizg on 15-2-3.
 */

public class VersionManager {

    public static final String KEY_VERSION_CODE = "key_version_code";
    public static final String KEY_NEED_CLEAR = "key_need_clear";
    public static final int FROYO_SDK_START = 8;

    public final static int GINGERBREAD_SDK_START = 9;

    public final static int ICS_SDK_START = 14;

    public final static int ICS_SDK_MR = 15; //android-4.0.3

    public final static int JB_SDK_START = 16;

    public final static int JB_SDK_MR1 = 17;

    public final static int JB_SDK_MR2 = 18;

    public final static int KIT_KAT_R1 = 19; //android-4.4_r1

    private final static int SUPPORT_SDK_START = FROYO_SDK_START;

    private final static int SUPPORT_SDK_END = 100;

    private int mOldVersionCode = 0;
    private int mCurrentVersionCode = 0;
    private static VersionManager sInstance;

    public static synchronized VersionManager getsInstance() {
        if (null == sInstance) {
            sInstance = new VersionManager();
        }
        return sInstance;
    }

    private VersionManager() {
        mCurrentVersionCode = getCurrentVersionCode(GlobalData.app());
        mOldVersionCode = PreferenceUtils.getSettingInt(GlobalData.app(), KEY_VERSION_CODE, 0);
        PreferenceUtils.setSettingInt(GlobalData.app(), KEY_VERSION_CODE, mCurrentVersionCode);
    }

    public static int getCurrentSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static boolean canRunOnCurrentSDKVersion() {
        int currentSdk = getCurrentSdkVersion();
        return SUPPORT_SDK_START <= currentSdk && currentSdk <= SUPPORT_SDK_END;
    }

    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
        }
        return "";
    }

    public static int getCurrentVersionCode(Context context) {
        int code = 0;
        try {
            code = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
        }
        return code;
    }

    public static boolean supportAllSystemLockscreenPassword() {
        return getCurrentSdkVersion() >= ICS_SDK_START;
    }

    public static boolean isLaterThanHoneycombMR2() {
        return getCurrentSdkVersion() >= ICS_SDK_START;
    }

    public static boolean isBeforeThanICS() {
        return getCurrentSdkVersion() < ICS_SDK_START;
    }

    public static boolean isICSSeries() {
        return getCurrentSdkVersion() >= ICS_SDK_START && getCurrentSdkVersion() < JB_SDK_START;
    }

    public static boolean isJellybean() {
        return getCurrentSdkVersion() == JB_SDK_START;
    }

    public static boolean isJellybeanAndMR1() {
        return getCurrentSdkVersion() == JB_SDK_START || getCurrentSdkVersion() == JB_SDK_MR1;
    }

    public static boolean isLaterThanJellybean() {
        return getCurrentSdkVersion() >= JB_SDK_START;
    }

    public static boolean isLaterThanJellybeanMR1() {
        return getCurrentSdkVersion() >= JB_SDK_MR1;
    }

    public static boolean isLaterThanJellybeanMR2() {
        return getCurrentSdkVersion() >= JB_SDK_MR2;
    }

    public static boolean isBeforeThanJellybeanMR2() {
        return getCurrentSdkVersion() < JB_SDK_MR2;
    }

    public static boolean isKitkatOrLater() {
        return getCurrentSdkVersion() >= KIT_KAT_R1;
    }

    public static boolean isKitkat() {
        return getCurrentSdkVersion() == KIT_KAT_R1;
    }

    public static boolean isJellybeanMR2() {
        return getCurrentSdkVersion() == JB_SDK_MR2;
    }

    public static String getReleaseChannel(Context context) {
        if (ReleaseChannelUtils.getReleaseChannel().equals(Constants.DEBUG_CHANNEL)) {
            return "debug";
        }
        return ReleaseChannelUtils.getReleaseChannel();
    }
}