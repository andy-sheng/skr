package com.base.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.log.MyLog;
import com.base.pinyin.HanziToPinyin;
import com.base.preference.PreferenceUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.language.LocaleUtil;
import com.base.utils.sdcard.SDCardUtils;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;

import junit.framework.Assert;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by MK on 15-4-13.
 */
public abstract class CommonUtils {
    private static final String TAG = CommonUtils.class.getSimpleName();

    private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;
    public static final int FAST_DOUBLE_CLICK_INTERVAL = 500;
    private static final String FILENAME_FORMAT = "%s_%d.%s";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");
    private static final Pattern HANZI_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");
    private static final Pattern HANZI_STRING_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]+");
    private static final Pattern LETTER_STRING_PATTERN = Pattern.compile("[A-Za-z]+");

    private static MyAlertDialog mDialog = null;

    public static final int LANGUAGE_OTHER = 0;
    public static final int LANGUAGE_CHINESE = 1;
    public static final int LANGUAGE_CHINESE_TAIWAN = 2;
    public static final int LANGUAGE_ENGLISH = 3;

    public static final String NO_PERMISSION_MD5 = "--NO--PERMISSION--";

    private final static String[] HEX_DIGITS = {"0", "1", "2", "3", "4",
            "5", "6", "7", "8", "9",
            "a", "b", "c", "d", "e", "f"};

    private static long sLastClickTime = 0;

    private static HashMap<Character, String> mPinyinCache = new HashMap<Character, String>();
    private static Map<String, String> mPolyPhoneWords = new HashMap<>();  // 包含多音字的词语

    static {
        mPolyPhoneWords.put("\u91cd\u5e86", "chongqin"); // 重庆
    }


    // 检测MIUI
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    /**
     * Regular expression for MIUI development version.
     */
    private static final String REGULAR_EXPRESSION_FOR_DEVELOPMENT = "\\d+.\\d+.\\d+(-internal)?";
    /**
     * Indicates the current MIUI is development edition. @hide
     */
    public static final boolean IS_DEVELOPMENT_VERSION = !TextUtils.isEmpty(Build.VERSION.INCREMENTAL) && Build.VERSION.INCREMENTAL.matches(REGULAR_EXPRESSION_FOR_DEVELOPMENT);
    /**
     * Indicates the current MIUI is stable edition. @hide
     */
    public static final boolean IS_STABLE_VERSION = "user".equals(Build.TYPE) && !IS_DEVELOPMENT_VERSION;

    public static final String MI_VIDEO_PACKAGE = "com.miui.video";

    public static boolean isHanzi(String str) {
        return HANZI_PATTERN.matcher(str).matches();
    }

    public static boolean isHanziStr(String str) {
        return HANZI_STRING_PATTERN.matcher(str).matches();
    }

    public static boolean isNumeric(String str) {
        return NUMBER_PATTERN.matcher(str).matches();
    }

    public static boolean isLetterStr(String str) {
        return LETTER_STRING_PATTERN.matcher(str).matches();
    }

    //默认500毫秒

    /**
     * 判断是否是快速点击
     */
    public static boolean isFastDoubleClick() {
        return isFastDoubleClick(FAST_DOUBLE_CLICK_INTERVAL);
    }

    /**
     * 判断是否是快速点击
     *
     * @param time, 时间间隔, 单位为毫秒
     * @return
     */
    public static boolean isFastDoubleClick(long time) {
        if (time <= 0) {
            return true;
        }

        long now = System.currentTimeMillis();
        long delta = now - sLastClickTime;
        if (delta > 0 && delta < time) {
            return true;
        }
        sLastClickTime = now;
        return false;
    }

    public static void startSystemAddContact(Context context, String telephoneNum) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setType("vnd.android.cursor.dir/person");
        intent.setType("vnd.android.cursor.dir/contact");
        intent.setType("vnd.android.cursor.dir/raw_contact");
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, telephoneNum);
        context.startActivity(intent);
    }

    public static void startPickSystemContact(Context context) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
    }

    @Deprecated
    public static boolean isChineseLocale(final Context context) {
        return Locale.CHINA.toString().equalsIgnoreCase(
                Locale.getDefault().toString())
                || Locale.CHINESE.toString().equalsIgnoreCase(
                Locale.getDefault().toString());
    }

    // This method is NOT thread safe!!! - Huahang Liu, Mar 2nd 2012
    public static String getUniqueFilePath(final File root, final String filename) {
        final File file = new File(root, filename);
        if (!file.exists()) {
            return file.getAbsolutePath();
        }
        final int dotPos = filename.lastIndexOf('.');
        String part1 = filename;
        String part2 = "";
        if (dotPos > 0) {
            part1 = filename.substring(0, dotPos);
            part2 = filename.substring(dotPos + 1);
        }
        for (int i = 1; true; i++) {
            final String res = String.format(FILENAME_FORMAT, part1, i, part2);
            final File t = new File(root, res);
            if (!t.exists()) {
                return t.getAbsolutePath();
            }
        }
    }


    /**
     * 通知系统扫描某个文件。
     *
     * @param context
     * @param fileName
     */
    public static void scanMediaFile(final Context context,
                                     final String fileName) {
        final Uri data = Uri.fromFile(new File(fileName));
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                data));
    }


    public static void highlightSubStr(final TextView view,
                                       String oriStr, String keyword,
                                       final int color) {
        int start = oriStr.indexOf(keyword);

        SpannableStringBuilder style = new SpannableStringBuilder(oriStr);
        style.setSpan(new ForegroundColorSpan(color), start, start + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        view.setText(style);
    }

    /**
     * 先对key进行preProcess，然后按照空格字符 split
     *
     * @param key
     * @return
     */
    public static CharSequence[] getCharSequenceArray(String key) {
        if (!TextUtils.isEmpty(key)) {
            String[] keywords = CommonUtils.preProcess(key).trim().split("\\s+");
            if (keywords != null) {
                CharSequence[] csAry = new CharSequence[keywords.length];
                for (int i = 0; i < keywords.length; i++) {
                    csAry[i] = keywords[i];
                }
                return csAry;
            }

        }
        return new CharSequence[]{
                key
        };
    }


    //会在非字母数字字符 和  字母数字字符之间加空格
    public static String preProcess(String key) {
        StringBuilder sb = new StringBuilder();
        if (key != null) {
            for (int i = 0; i < key.length(); i++) {
                char ch = key.charAt(i);
                sb.append(ch);
                if (i + 1 < key.length()) {
                    char nc = key.charAt(i + 1);
                    if ((isLetterOrDigit(ch) && !isLetterOrDigit(nc))
                            || (!isLetterOrDigit(ch) && isLetterOrDigit(nc))) {
                        sb.append(" ");
                    }
                }
            }
        }
        return sb.toString();
    }

    public static boolean isLetterOrDigit(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ('0' <= ch && ch <= '9');
    }

    public static boolean isLetterOrDigit(String str) {
        if (TextUtils.isEmpty(str))
            return false;
        for (int i = 0; i < str.length(); i++) {
            if (!isLetterOrDigit(str.toCharArray()[i])) {
                return false;
            }
        }
        return true;
    }

    public static String getCountryISO(final Context context) {
        String country = PreferenceUtils.getSettingString(context,
                "country", "");
        if (TextUtils.isEmpty(country)) {
            country = com.base.utils.CommonUtils.getCountryISOFromSimCard(context);
            if (country == null) {
                country = "";
            }
            if (!TextUtils.isEmpty(country)) {
                PreferenceUtils.setSettingString(context,
                        "country", country);
            }
        }
        return country.toUpperCase();
    }

    public static boolean isIntentAvailable(final Context context,
                                            final String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        final List<ResolveInfo> list = packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static boolean isIntentAvailable(final Context context,
                                            final Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> list = packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    static boolean sIsMIUI = false;

    static {
        Intent i = new Intent("miui.intent.action.APP_PERM_EDITOR");
        if (isIntentAvailable(GlobalData.app(), i)) {
            sIsMIUI = true;
        }
    }

    static boolean sIsGlobalMIUI = System.getProperty("ro.product.mod_device", "").contains("global");

    static boolean sIsMIUIRom;

    static {
        try {
            sIsMIUIRom = (GlobalData.app().getPackageManager().getPackageInfo(
                    "com.miui.cloudservice", PackageManager.GET_CONFIGURATIONS) != null);
        } catch (final PackageManager.NameNotFoundException e) {
            sIsMIUIRom = false;
        }
    }

    // 是否是国际版miui
    public static boolean isMIUIGlobal() {
        return sIsGlobalMIUI;
    }

    public static boolean isMIUI() {
        return sIsMIUI;
    }

    public static boolean isMIUIRom() {
        return sIsMIUIRom;
    }

    //获取手机系统版本号
    public static String getSystemCode() {
        return Build.VERSION.INCREMENTAL;
    }

    public static boolean isMIUI8() {
        Properties prop = new Properties();
        try {
            InputStream is = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
            prop.load(is);

            String versionName = prop.getProperty(KEY_MIUI_VERSION_NAME, null);
            MyLog.w(TAG, "LiveActivity versionName=" + versionName);
            if (is != null)
                is.close();
            if (versionName.startsWith("V")) {
                versionName = versionName.replace("V", "");
                if (Integer.parseInt(versionName) >= 8) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //判断当前手机系统版本号和规定版本号大小
    public static boolean isEqualOrLargerForMiuiSysCode(String minSysCode) {
        //本机系统版本号
        String localSystemCode = getSystemCode();
        if (TextUtils.isEmpty(localSystemCode) || TextUtils.isEmpty(minSysCode)) {
            MyLog.w(TAG, "localSystemCode or minSysCode is empty");
            return false;
        }
        String[] localSystemCodeSplitList = localSystemCode.split("\\.");
        String[] minCodeSplitList = minSysCode.split("\\.");
        int minCodeSplitListLength = minCodeSplitList.length;
        if (localSystemCodeSplitList.length != minCodeSplitListLength) {
            //不相等，沒有可比性
            MyLog.w(TAG, "localSystemCodeSplitList.length != minCodeSplitListLength");
            return false;
        }
        try {
            for (int i = 0; i < minCodeSplitListLength; ++i) {
                int minCodeTemp = Integer.parseInt(minCodeSplitList[i]);
                int localSystemCodeTemp = Integer.parseInt(localSystemCodeSplitList[i]);
                if (minCodeTemp > localSystemCodeTemp) {
                    MyLog.w(TAG, "minSysCode is larger than localSysCode");
                    return false;
                } else if (minCodeTemp < localSystemCodeTemp) {
                    MyLog.w(TAG, "localSysCode is larger than minSysCode");
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
        MyLog.w(TAG, "localSysCode is equal of minSysCode");
        return true;
    }


    public static String getImageAutoScaleSize(String url, int imageSize) {
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        sb.append("?thumb=");
        sb.append(imageSize);
        sb.append("x");
        sb.append(imageSize);
        sb.append("&scale=auto");
        return sb.toString();
    }

    public static String getFileFromSD(String dir, String filename) {
        if (TextUtils.isEmpty(dir) || TextUtils.isEmpty(filename)) {
            return null;
        }
        if (SDCardUtils.isSDCardBusy()) {
            MyLog.e("sdcard is busy, cannot get external cache dir");
            return null;
        }
        String result = "";
        File file = new File(dir, filename);

        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            fis = new FileInputStream(file);
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, count);
            }
            result = baos.toString();
        } catch (FileNotFoundException e) {
            MyLog.e("getFileFromSD not found" + e);
        } catch (IOException e) {
            MyLog.e("getFileFromSD io failure" + e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return result;
    }

    // 从assets 文件夹中获取文件并读取数据
    public static String getFromAssets(final Context context,
                                       final String fileName, final String encoding) {
        String result = "";
        final byte[] buffer = new byte[8192];
        try {

            final InputStream in = context.getResources().getAssets()
                    .open(fileName);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (true) {
                // 将文件中的数据读到byte数组中
                final int size = in.read(buffer);
                if (size != -1) {
                    baos.write(buffer, 0, size);
                } else {
                    break;
                }
            }
            in.close();
            result = baos.toString();
            baos.close();
        } catch (final IOException e) {
            MyLog.e("getFromAssets 读取文件错误" + fileName);
        }
        return result;
    }

    public static CharSequence addClickableSpan(final String source,
                                                final String key, final View.OnClickListener clickListener,
                                                final boolean underline, final int colorResId) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder(source);
        final int index = source.indexOf(key);
        if (index >= 0) {
            ssb.setSpan(new ClickableSpan() {

                @Override
                public void onClick(final View widget) {
                    clickListener.onClick(widget);
                }
            }, index, index + key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new CharacterStyle() {

                @Override
                public void updateDrawState(final TextPaint tp) {
                    tp.setUnderlineText(underline);
                    tp.setColor(GlobalData.app().getResources()
                            .getColor(colorResId));
                }
            }, index, index + key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ssb;
    }

    // 判断是否是锁屏页面
    public static boolean isScreenLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) GlobalData.app().getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    public static boolean isAudioVoiceCallDisabled() {
        return Build.MODEL.contains("ZTE");
    }

    /**
     * 默认使用VOICE_CALL的MODEL
     */
    final static String[] AUDIO_MODE_VOICE_CALL = {"MI NOTE Pro"};

    public static int getAudioModeByModel() {
        for (final String device : AUDIO_MODE_VOICE_CALL) {
            if (device.equals(Build.MODEL)) {
                return AudioManager.STREAM_VOICE_CALL;
            }
        }
        return AudioManager.STREAM_MUSIC;
    }

    /*
    * 由于某些机器的proximity.getresolution获得的值永远小于sensor event value， 无法使用距离感应器。本地机器是否应该使用距离感应器。
    */
    public static boolean shouldAvoidProximitySensor() {
        final String[] unsupportedDeviceModels = new String[]{
                "MB525", "ME525", "ME525+", "ME722", "ME811", "MotoA953", "HS-U8", "HS-E910", "S8600", "EG900",
                "HS-EG900"
        };

        final String[] unsupportedDeviceBrands = new String[]{
                "moto"
        };

        for (final String device : unsupportedDeviceModels) {
            if (device.equals(Build.MODEL)) {
                return true;
            }
        }

        if (Build.MODEL.startsWith("HS-")) {
            return true;
        }

        for (final String device : unsupportedDeviceBrands) {
            if (device.equals(Build.BRAND)) {
                return true;
            }
        }

        return false;
    }

    public static int getLengthLimitIndex(final String source,
                                          final String dest, final int maxLengthLimit) {
        int index = -1;
        if (source == null) {
            return index;
        }

        int destCount = getLengthDistinguishChinese(dest);
        int sourceCount = 0;
        for (int i = 0; i < source.length(); i++) {
            if (isASCII(source.charAt(i))) {
                sourceCount += 1;
            } else if (isChineseChar(source.charAt(i))) {
                sourceCount += 2;
            } else {
                sourceCount += 2;
            }
            if (destCount + sourceCount > maxLengthLimit) {
                index = i;
                break;
            }
        }

        return index;
    }

    public static int getLengthDistinguishChinese(final String str) {
        if (str == null) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (isASCII(str.charAt(i))) {
                count += 1;
            } else if (isChineseChar(str.charAt(i))) {
                count += 2;
            } else {
                count += 2;
            }
        }
        return count;
    }

    /**
     * 是否是汉字
     */
    private static boolean isChineseChar(char c) {
        return (c >= 0x4e00) && (c <= 0x9fa5);
    }

    // 判断是否是ASCII码
    public static boolean isASCII(char c) {
        return c >= 0x00 && c <= 0x7f;
    }

    /**
     * 是否是中日韩文或者其全角标点符号
     */
    private static boolean isChineseJapaneseKorea(char c) {
        return (c >= 0x4e00) && (c <= 0x9fcc) || (c >= 0x3000) && (c <= 0x303f)
                || (c >= 0xff01) && (c <= 0xff5e);
    }

    /**
     * 拷贝文件
     *
     * @param oldPath
     * @param newPath
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int byteRead = 0;
            File oldFile = new File(oldPath);
            if (oldFile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[8192];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
                fs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isM4Device() {
        return Build.MODEL.contains("MI") && Build.MODEL.contains("4");
    }

    public static boolean isMiNoteDevice() {
        return Build.MODEL.contains("MI") && Build.MODEL.contains("NOTE");
    }

    public static boolean isMeizu() {
        return "Meizu".equals(Build.MANUFACTURER);
    }

    public static String getDeviceModePrefix() {
        if (isM4Device()) {
            return "MI4_";
        } else if (isMiNoteDevice()) {
            return "MINOTE_";
        }
        return "";
    }

    public static int getStatusBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean isXMPhone() {
        return Build.MODEL.contains("mione") || Build.MODEL.contains("MI-ONE") || Build.MODEL.startsWith("MI")
                || Build.MANUFACTURER.equalsIgnoreCase("Xiaomi") || Build.BRAND.equalsIgnoreCase("Xiaomi");
    }

    /**
     * 返回一个smtp的名字部分 返回account的帐号部分，如166160@xiaomi.com, 返回166160， 如果传入是166160， 那么返回的就直接是166160.
     *
     * @param smtp
     * @return
     */
    public static String getSmtpLocalPart(final String smtp) {
        if (TextUtils.isEmpty(smtp)) {
            return null;
        }

        final int iAt = smtp.indexOf("@");
        if (iAt > 0) {
            return smtp.substring(0, iAt);
        }

        return smtp;
    }

    public static String getCurrentWifiMacAddress(final Context context) {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wInfo = wifiManager.getConnectionInfo();
        if (wInfo != null) {
            return wInfo.getMacAddress();
        }
        return null;
    }

    public static String getCountryISOFromSimCard(final Context context) {
        final TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimCountryIso();
    }

    @Deprecated
    public static boolean isChineseLocale() {
        return Locale.CHINA.toString().equalsIgnoreCase(
                Locale.getDefault().toString())
                || Locale.CHINESE.toString().equalsIgnoreCase(
                Locale.getDefault().toString());
    }

    public static int getLanguageInfo() {
        Locale lan = Locale.getDefault();
        String language = lan.getLanguage();
        String country = lan.getCountry().toLowerCase();
        if ("zh".equals(language)) {
            if ("cn".equals(country)) {
                return LANGUAGE_CHINESE;
            } else if ("tw".equals(country)) {
                return LANGUAGE_CHINESE_TAIWAN;
            }
        } else if ("uk".equals(language) || "us".equals(language) || "en".equals(language)) {
            return LANGUAGE_ENGLISH;
        }
        return LANGUAGE_OTHER;
    }


    public static boolean isChineseSimCard(final Context context) {
        return "CN".equalsIgnoreCase(getCountryISOFromSimCard(context));
    }


    public static boolean isLetter(final char c) {
        return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'));
    }

    public static byte[] readFromFile(String fileName, int offset, int len) {
        if (fileName == null) {
            return null;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            Log.i(TAG, "readFromFile: file not found");
            return null;
        }

        if (len == -1) {
            len = (int) file.length();
        }

        Log.d(TAG, "readFromFile : offset = " + offset + " len = " + len + " offset + len = " + (offset + len));

        if (offset < 0) {
            Log.e(TAG, "readFromFile invalid offset:" + offset);
            return null;
        }
        if (len <= 0) {
            Log.e(TAG, "readFromFile invalid len:" + len);
            return null;
        }
        if (offset + len > (int) file.length()) {
            Log.e(TAG, "readFromFile invalid file len:" + file.length());
            return null;
        }

        byte[] b = null;
        try {
            RandomAccessFile in = new RandomAccessFile(fileName, "r");
            b = new byte[len];
            in.seek(offset);
            in.readFully(b);
            in.close();

        } catch (Exception e) {
            Log.e(TAG, "readFromFile : errMsg = " + e.getMessage());
            e.printStackTrace();
        }
        return b;
    }

    public static Bitmap extractThumbNail(final String path, final int height, final int width, final boolean crop) {
        Assert.assertTrue(path != null && !path.equals("") && height > 0 && width > 0);

        BitmapFactory.Options options = new BitmapFactory.Options();

        try {
            options.inJustDecodeBounds = true;
            Bitmap tmp = BitmapFactory.decodeFile(path, options);
            if (tmp != null) {
                tmp.recycle();
                tmp = null;
            }

            Log.d(TAG, "extractThumbNail: round=" + width + "x" + height + ", crop=" + crop);
            final double beY = options.outHeight * 1.0 / height;
            final double beX = options.outWidth * 1.0 / width;
            Log.d(TAG, "extractThumbNail: extract beX = " + beX + ", beY = " + beY);
            options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY) : (beY < beX ? beX : beY));
            if (options.inSampleSize <= 1) {
                options.inSampleSize = 1;
            }

            // NOTE: out of memory error
            while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
                options.inSampleSize++;
            }

            int newHeight = height;
            int newWidth = width;
            if (crop) {
                if (beY > beX) {
                    newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
                } else {
                    newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
                }
            } else {
                if (beY < beX) {
                    newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
                } else {
                    newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
                }
            }

            options.inJustDecodeBounds = false;

            Log.i(TAG, "bitmap required size=" + newWidth + "x" + newHeight + ", orig=" + options.outWidth + "x" + options.outHeight + ", sample=" + options.inSampleSize);
            Bitmap bm = BitmapFactory.decodeFile(path, options);
            if (bm == null) {
                Log.e(TAG, "bitmap decode failed");
                return null;
            }

            Log.i(TAG, "bitmap decoded size=" + bm.getWidth() + "x" + bm.getHeight());
            final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
            if (scale != null && !scale.equals(bm)) { //scale != bm
                bm.recycle();
                bm = scale;
            }

            if (crop) {
                final Bitmap cropped = Bitmap.createBitmap(bm, (bm.getWidth() - width) >> 1, (bm.getHeight() - height) >> 1, width, height);
                if (cropped == null || cropped.equals(bm)) {
                    return bm;
                }

                bm.recycle();
                bm = cropped;
                Log.i(TAG, "bitmap croped size=" + bm.getWidth() + "x" + bm.getHeight());
            }
            return bm;

        } catch (final OutOfMemoryError e) {
            Log.e(TAG, "decode bitmap failed: " + e.getMessage());
            options = null;
        }
        return null;
    }

    public static Bitmap getBitMapByWebUrl(final String url) {
        try {
            if (TextUtils.isEmpty(url)) {
                return null;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(getImageStream(url));
            return bitmap;
        } catch (Exception e) {
            MyLog.e("e");
            return null;
        }
    }


    public static String getShareImageLocalPath(String fileName, int drawableId) {


        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        String saveDir = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE/pic";
        String filePath = saveDir + "/" + fileName + ".JPEG";
        File file = new File(filePath);
        MyLog.d(TAG, "PicDir = " + file.getPath());
        if (file.exists()) {
            return file.getPath();
        }
        Resources res = GlobalData.app().getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, drawableId);
        return CommonUtils.savePicInLocalCertainPath(bitmap, filePath);
    }

    public static String downloadImg(final String url) {
        try {
            if (TextUtils.isEmpty(url)) {
                return "";
            }
            Bitmap bitmap = BitmapFactory.decodeStream(getImageStream(url));
            String imgUrl = savePicInLocal(bitmap);
            return imgUrl;
        } catch (Exception e) {
            MyLog.e("e");
            return "";
        }
    }

    /**
     * Get image from newwork
     *
     * @param path The path of image
     * @return InputStream
     * @throws Exception
     */
    public static InputStream getImageStream(String path) throws Exception {
        MyLog.d(TAG, "getImageStream");
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return conn.getInputStream();
        }
        return null;
    }

    public static String savePicInLocalCertainPath(final Bitmap bitmap, String path) {
        MyLog.d(TAG, "SavePicInLocal");
        if (bitmap == null || TextUtils.isEmpty(path)) {
            return null;
        }
        String saveDir = Environment.getExternalStorageDirectory()
                + "/Xiaomi/WALI_LIVE/pic";
//        String fileName = saveDir + "/" + path + ".JPEG";
        File file = new File(path);
        MyLog.d(TAG, "PicDir = " + file.getPath());
        if (file.exists()) {
            return file.getPath();
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ByteArrayOutputStream baos = null; // 字节数组输出流
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteArray = baos.toByteArray();// 字节数组输出流转换成字节数组

            File dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdir(); // 创建文件夹
            }

            file.createNewFile();// 创建文件
            MyLog.e("PicDir", file.getPath());

            // 将字节数组写入到刚创建的图片文件中
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(byteArray);
            MyLog.d(TAG, "保存成功 file.getPath()=" + file.getPath());
            return file.getPath();
        } catch (Exception e) {
            MyLog.e(TAG, "Exception = " + e);
            return null;
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 保存图片到手机的sd卡
    private static String savePicInLocal(final Bitmap bitmap) {
        MyLog.d(TAG, "SavePicInLocal");
        if (bitmap == null) {
            return "";
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ByteArrayOutputStream baos = null; // 字节数组输出流
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteArray = baos.toByteArray();// 字节数组输出流转换成字节数组
            String saveDir = Environment.getExternalStorageDirectory()
                    + "/Xiaomi/WALI_LIVE/pic";
            File dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdir(); // 创建文件夹
            }
            String fileName = saveDir + "/" + System.currentTimeMillis() + ".JPEG";
            File file = new File(fileName);
            MyLog.d(TAG, "PicDir = " + file.getPath());
            if (!file.exists()) {
                file.createNewFile();// 创建文件
                MyLog.e("PicDir", file.getPath());
            }
            // 将字节数组写入到刚创建的图片文件中
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(byteArray);
            MyLog.d(TAG, "保存成功 file.getPath()=" + file.getPath());
            return file.getPath();
        } catch (Exception e) {
            MyLog.e(TAG, "Exception = " + e);
            return "";
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * @param root
     * @param filename
     * @return
     */
    // This method is NOT thread safe!!! - Huahang Liu, Mar 2nd 2012
    public synchronized static String getUniqueFileName(final File root,
                                                        final String filename) {
//        final File file = new File(root, filename);
//        if (!file.exists()) {
//            return file.getAbsolutePath();
//        }
        final int dotPos = filename.lastIndexOf('.');
        String part1 = filename;
        String part2 = "";
        if (dotPos > 0) {
            part1 = filename.substring(0, dotPos);
            part2 = filename.substring(dotPos + 1);
        }
        long currentTime = System.currentTimeMillis();
        for (int i = 1; true; i++) {
            final String res = String.format(FILENAME_FORMAT, part1, currentTime, part2);
            final File t = new File(root, res);
            if (!t.exists()) {
                return t.getAbsolutePath();
            }
        }
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
//            e.printStackTrace();
        }
        return packageInfo != null;
    }

    public static boolean checkApkExist(Context context, String packageName) {

        if (packageName == null || "".equals(packageName)) {
            return false;
        }

        final PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName)) {
                return true;
            }

        }
        return false;
    }

    public static boolean isRecyclerViewTop(RecyclerView mRecyclerView) {
        RecyclerView.LayoutManager mLayoutManager = mRecyclerView.getLayoutManager();
        if (mLayoutManager != null && mLayoutManager instanceof LinearLayoutManager) {
            if (((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition() == 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAppForeground(final Context context) {
        String packageName = context.getPackageName();
        if (Build.VERSION.SDK_INT > 20) {
            long ts = System.currentTimeMillis();
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 5000, ts);

            if (queryUsageStats != null && !queryUsageStats.isEmpty()) {

                UsageStats recentStats = null;
                for (UsageStats usageStats : queryUsageStats) {
                    if (recentStats == null || recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                        recentStats = usageStats;
                    }
                }
                return recentStats.getPackageName().equalsIgnoreCase(packageName);
            }
        } else {
            final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            final List<RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (tasks != null && !tasks.isEmpty()) {
                final ComponentName topActivity = tasks.get(0).topActivity;
                if (topActivity != null && topActivity.getPackageName().equals(packageName)) {
                    return true;
                }
            }
        }
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        if (appProcesses != null && appProcesses.size() > 0) {

            for (RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(packageName)
                        && (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        //||appProcess.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE
                )
                        ) {
                    return true;
                }
            }
        }
//        if(Build.VERSION.SDK_INT>22){
//            List<ActivityManager.RecentTaskInfo> recentTasks = am.getRecentTasks(1,ActivityManager.RECENT_WITH_EXCLUDED);
//            if (recentTasks!= null && !recentTasks.isEmpty()) {
//                final ComponentName topActivity = recentTasks.get(0).topActivity;
//                if (topActivity!=null && topActivity.getPackageName().equals(packageName)) {
//                    return true;
//                }
//            }
//        }
        return false;
    }

    public static String getTopActivityName(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks != null && !tasks.isEmpty()) {
            String packageName = context.getPackageName();
            final ComponentName topActivity = tasks.get(0).topActivity;
            if (topActivity != null && topActivity.getPackageName().equals(packageName)) {
                String[] names = topActivity.getClassName().split("\\.");
                return names[names.length - 1];
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    public static boolean match(String value, String keyword) {
        if (value == null || keyword == null)
            return false;
        if (keyword.length() > value.length())
            return false;

        int i = 0, j = 0;
        do {
            if (keyword.charAt(j) == value.charAt(i)) {
                i++;
                j++;
            } else if (j > 0)
                break;
            else
                i++;
        } while (i < value.length() && j < keyword.length());

        return (j == keyword.length()) ? true : false;
    }

    public static String getHttpUrlFromCache(String httpImgUrl) {
        File file = FrescoWorker.getCacheFileFromFrescoDiskCache(httpImgUrl);
        if (file != null) {
            return file.getPath();
        }
        return "";//todo 这个地方为何不返回httpImgUrl呢？
    }

    public static boolean isLocalChina() {
        String country = "zh_CN";
        Locale locale = LocaleUtil.getLocale(); //涉及本地app语言和系统语言两种
        if (locale != null) {
            country = TextUtils.isEmpty(locale.toString()) ? country : locale.toString();
        }
        return TextUtils.isEmpty(country) || country.toUpperCase().equals("ZH_CN");
    }

    /**
     * 台湾和大陆
     */
    public static boolean isChinese() {
        String country = "zh_CN";
        Locale locale = LocaleUtil.getLocale(); //涉及本地app语言和系统语言两种
        if (locale != null) {
            country = TextUtils.isEmpty(locale.toString()) ? country : locale.toString();
        }
        return TextUtils.isEmpty(country) || country.toUpperCase().equals("ZH_CN") || country.toUpperCase().equals("ZH_TW");
    }

    /**
     * 当前应用语言是否为简体中文
     *
     * @return
     */
    public static boolean isAppInSimplifiedChineseLocale() {
        return Locale.SIMPLIFIED_CHINESE.toString().equalsIgnoreCase(LocaleUtil.getLanguageCode());
    }

    public static boolean isInternationalPayMode() {
        return !(isAppInSimplifiedChineseLocale() && !Constants.isGooglePlayBuild);
    }

    public static void toHome() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        GlobalData.app().startActivity(i);
    }

    public static final String getFileMD5(String filename) {

        InputStream fis;
        try {
            fis = new FileInputStream(filename);
        } catch (Exception e) {
            MyLog.w("", e);
            if (null != e.getMessage() && e.getMessage().contains("(Permission denied)")) {//没有权限引起的访问失败
                return NO_PERMISSION_MD5;
            } else {
                return null;
            }
        }

        return getFileStreamMd5(fis);
    }

    public static final String getFileStreamMd5(InputStream fis) {
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5;

        try {
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
        } catch (NoSuchAlgorithmException e) {
            MyLog.e("", e);
            return null;
        } catch (Exception e) {
            MyLog.e("", e);
            return null;
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                MyLog.e("", e);
            }
        }
        if (md5 != null) {
            return byteArrayToString(md5.digest());
        } else {
            return null;
        }
    }

    private static String byteArrayToString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return HEX_DIGITS[d1] + HEX_DIGITS[d2];
    }

    private static final String SCHEME = "package";
    /**
     * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
     */
    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
    /**
     * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
     */
    private static final String APP_PKG_NAME_22 = "pkg";
    /**
     * InstalledAppDetails所在包名
     */
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    /**
     * InstalledAppDetails类名
     */
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    /**
     * 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息。 对于Android 2.3（Api Level
     * 9）以上，使用SDK提供的接口； 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）。
     *
     * @param context
     * @param packageName 应用程序的包名
     */
    public static void showInstalledAppDetails(Context context, String packageName) {

        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) {
            // 2.3（ApiLevel 9）以上，使用SDK提供的接口
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else {
            // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
            // 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap convertViewToBitmap(View view) {
        if (view == null) {
            MyLog.d("convertViewToBitmap", "view == null");
            return null;
        }
        //view.measure(View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        //view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        if (bitmap == null) {
            MyLog.d("convertViewToBitmap", "bitmap == null");
        } else {
            bitmap = Bitmap.createBitmap(bitmap);
        }
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }


    public static String printPBDataLog(GeneratedMessage message) {
        Map fileds = message.getAllFields();
        StringBuilder printData = new StringBuilder();
        Set<Descriptors.FieldDescriptor> keySet = fileds.keySet();
        for (Descriptors.FieldDescriptor key : keySet) {
            Object data = fileds.get(key);
            printData.append(key.getName() + "：");
            if (data instanceof GeneratedMessage) {
                printData.append("[");
                printData.append(printPBDataLog((GeneratedMessage) data));
                printData.append("]");
            } else if (data instanceof List) {
                printData.append("[");
                for (Object item : (List) data) {
                    if (data instanceof GeneratedMessage) {
                        printData.append(printPBDataLog((GeneratedMessage) data));
                    } else {
                        printData.append(item);
                    }
                }
                printData.append("]");
            } else {
                printData.append(data);
            }
            printData.append(";");

        }
        return printData.toString();
    }

    public static final String VIDEO_DIR = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE/video";

    public static String getCompressFilePath(String localPath) {
        String path;
        String mDir = VIDEO_DIR;
        File dir = new File(mDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        path = mDir + localPath.substring(localPath.lastIndexOf("/"), localPath.length());
        return path;
    }


    public static void clearCompressFiles() {
        File dir = new File(VIDEO_DIR);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
            }
        }
    }


    public static long getDuration(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            return Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            retriever.release();
        }
        return 0;
    }

    // Note: 返回的Duration以毫秒为单位
    public static Pair<Integer, Integer> getVideoInf(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));//宽
            int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));//高
            if (width > height) {
                width = height;
            }
            return Pair.create(Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)), width);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            retriever.release();
        }
        return new Pair(-1, -1);
    }

    public static int checkAPP(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(),
                            PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];

            int hashcode = sign.hashCode();
            MyLog.d("checkAPP", "checkAPP hashCode : " + hashcode);
            return hashcode == -82892576 ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取系统登录的小米帐号
     *
     * @return 小米账号的字符串形式，例如:"12345"
     */
    @Nullable
    public static String getSysMiAccount() {
        try {
            AccountManager am = AccountManager.get(GlobalData.app().getApplicationContext());
            Account[] accounts = am.getAccountsByType("com.xiaomi");
            if (accounts != null && accounts.length > 0) {
                return accounts[0].name;
            }
            return null;
        } catch (Exception e) {
            MyLog.e(TAG, e);
            return null;
        }
    }

    public static void copy(String content, Context context) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    public static void setScreenBrightness(Activity activity, float value) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams mParams = window.getAttributes();
        mParams.screenBrightness = value / 255.0F;
        window.setAttributes(mParams);
    }

    public static int getStreamMaxVolume() {
        AudioManager audioManager = (AudioManager) GlobalData.app().getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public static void setStreamVolume(int volume) {
        AudioManager audioManager = (AudioManager) GlobalData.app().getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (volume < 0) {
            volume = 0;
        } else if (volume > maxVolume) {
            volume = maxVolume;
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    /**
     * 参数：maxWidth 最大宽度
     * 参数：content  指TextView中要显示的内容
     */
    public static void setMaxEclipse(final TextView textView, int maxWidth, final String content) {
        TextPaint textPaint = textView.getPaint();
        float textPaintWidth = textPaint.measureText(content);

        if (textPaintWidth > maxWidth && content.length() > 6) {
            setMaxEclipse(textView, maxWidth, content.substring(0, content.length() - 6) + "...");
        } else {
            textView.setText(content);
        }
    }

    public static int measureWidth(Context context, String content) {
        TextView textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, DisplayUtils.dip2px(12f));
        TextPaint paint = textView.getPaint();
        int width = (int) paint.measureText(content);
        return width + DisplayUtils.dip2px(30);
    }

    /**
     * 检查查询前台应用权限
     */
    private static boolean checkQueryUsageStats(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            long ts = System.currentTimeMillis();
            UsageStatsManager usageStatsManager =
                    (UsageStatsManager) activity.getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST, 0, ts);
            return queryUsageStats != null && !queryUsageStats.isEmpty();
        }
        return true;
    }

    private static void openSettingPanel(@NonNull Activity activity) {
        PackageManager packageManager = activity.getPackageManager();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        List<ResolveInfo> retList =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (retList.isEmpty()) {
            // 若无法跳转到ACTION_USAGE_ACCESS_SETTINGS，则跳转到ACTION_SETTINGS
            intent = new Intent(Settings.ACTION_SETTINGS);
        }
        activity.startActivity(intent);
    }

    /**
     * 获取前台应用的包名
     */
    public static String getForegroundPackageName(@NonNull Activity activity) {
        if (!checkQueryUsageStats(activity)) {
            openSettingPanel(activity);
            return "no perm";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MyLog.d(TAG, "getForegroundPackageName after LOLLIPOP");
            UsageStatsManager usageStatsManager =
                    (UsageStatsManager) activity.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> statsList =
                    usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, time);
            if (statsList != null && !statsList.isEmpty()) {
                UsageStats usageStats = statsList.get(0);
                for (UsageStats elem : statsList) {
                    if (usageStats.getLastTimeUsed() < elem.getLastTimeUsed()) {
                        usageStats = elem;
                    }
                }
                return usageStats.getPackageName();
            }
        } else {
            MyLog.d(TAG, "getForegroundPackageName before LOLLIPOP");
        }
        return null;
    }

    /**
     * 把钱数由分变为元，例如1分变为0.01，20分变为0.2，123分变为1.23
     *
     * @param cent 分的数量
     * @return
     */
    public static String getHumanReadableMoney(int cent) {
        if (cent == 0) {
            return "0";
        }
        String s = String.valueOf(cent);
        switch (s.length()) {
            case 1:
                return "0.0" + s;
            case 2:
                s = "0." + s;
                break;
            default:
                s = s.substring(0, s.length() - 2) + "." + s.substring(s.length() - 2);
                break;
        }
        while (s.endsWith("0")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.endsWith(".")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * 通常用于把只包含一个数字的字符串中的数字变成指定颜色<br>
     * 只会把第一次出现高亮
     *
     * @param text       源字符串
     * @param keyword    需要高亮的字符串
     * @param colorResId
     * @return
     */
    public static CharSequence getHighLightKeywordText(@NonNull String text, @NonNull String keyword, @ColorRes int colorResId) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        if (TextUtils.isEmpty(keyword)) {
            return text;
        }
        int start = text.indexOf(keyword);
        if (start < 0) {
            return text;
        }
        int end = start + keyword.length();
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        ssb.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(colorResId)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    public static void setMargins(@NonNull View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.setLayoutParams(p);
        }
    }

    public static final String SP_FILE_NAME_MIWALLET = "miwallet";
    public static final String SP_KEY_MIWALLET_LOGIN_ACCOUNT_TYPE = "login.account.type";// 第一次使用小米钱包时，做出的选择，1：选择使用系统小米账号，2：没有选择使用系统小米账号
    public static final int LOGIN_ACCOUNT_TYPE_NONE = 0;
    public static final int LOGIN_ACCOUNT_TYPE_SYSTEM = 1;
    public static final int LOGIN_ACCOUNT_TYPE_OTHER = 2;

    public static int getMiWalletLoginAccountType() {
        return PreferenceUtils.getSettingInt(GlobalData.app().getSharedPreferences(SP_FILE_NAME_MIWALLET, Context.MODE_PRIVATE),
                SP_KEY_MIWALLET_LOGIN_ACCOUNT_TYPE, LOGIN_ACCOUNT_TYPE_NONE);
    }

    public static void setMiWalletLoginAccountType(int loginAccountType) {
        PreferenceUtils.setSettingInt(GlobalData.app().getSharedPreferences(SP_FILE_NAME_MIWALLET, Context.MODE_PRIVATE),
                SP_KEY_MIWALLET_LOGIN_ACCOUNT_TYPE, loginAccountType);
    }

    //排序
    public static int sortName(final String LocalName1, final String LocalName2) {
//
//        String pinyin1 = PinyinUtils.getFirstHanziPinyinByName(LocalName1);
//        String pinyin2 = PinyinUtils.getFirstHanziPinyinByName(LocalName2);
        String pinyin1 = getPinyinByName(LocalName1);
        String pinyin2 = getPinyinByName(LocalName2);
        pinyin1 = null != pinyin1 ? pinyin1 : "";
        pinyin2 = null != pinyin2 ? pinyin2 : "";
        if (pinyin1.contains("#") && !pinyin2.contains("#")) {
            return 1;
        }
        if (!pinyin1.contains("#") && pinyin2.contains("#")) {
            return -1;
        }

        return Collator.getInstance().compare(pinyin1, pinyin2);
    }

    public static String getPinyinByName(final String name) {
        if (name == null) {
            return "#";
        }
        final String noSpace = name.trim();
        if (TextUtils.isEmpty(noSpace)) {
            return "#";
        }
        final char firstChar = noSpace.charAt(0);
        if (mPinyinCache.containsKey(firstChar)) {
            try {
                return mPinyinCache.get(firstChar);
            } catch (final NullPointerException e) {
                //throw new NullPointerException("the hashmap is " + mPinyinCache + ", the key:"
                //+ firstChar + ", the value:" + mPinyinCache.get(firstChar));
                MyLog.e(e);
            }
        }
        String pinYin = hanziToPinyin(String.valueOf(firstChar)).toUpperCase();
        if (TextUtils.isEmpty(pinYin)) {
            pinYin = "#";
        } else {
            String[] pinyins = pinYin.split(" ");
            if (pinyins.length > 1 && noSpace.length() > 1) {
                String sourceWords = noSpace.substring(0, 2);
                if (getPolyphonePinyin(sourceWords) != null) {
                    pinYin = getPolyphonePinyin(sourceWords).toUpperCase();
                }
            }
            final char firstLetter = pinYin.charAt(0);
            if ((firstLetter < 'A') || (firstLetter > 'Z')) {
                pinYin = "#";
            }
        }
        mPinyinCache.put(firstChar, pinYin);
        return pinYin;
    }

    public static String hanziToPinyin(final String source) {
        final StringBuilder sbFullPinyin = new StringBuilder();
        if (Build.VERSION.SDK_INT < 14) {
            final ArrayList<HanziToPinyin.Token> pinyins = HanziToPinyin.getInstance().get(source);

            if ((pinyins != null) && (pinyins.size() > 0)) {
                for (final HanziToPinyin.Token aToken : pinyins) {
                    sbFullPinyin.append(aToken.target);
                    sbFullPinyin.append(" ");
                }
            }
        } else {
            final ArrayList<HanziToPinyin.Token> pinyins = HanziToPinyin.getInstance().get(source);

            if ((pinyins != null) && (pinyins.size() > 0)) {
                for (final HanziToPinyin.Token aToken : pinyins) {
                    if (aToken.polyPhones != null) {
                        for (int i = 0; i < aToken.polyPhones.length; i++) {
                            sbFullPinyin.append(aToken.polyPhones[i]);
                            sbFullPinyin.append(" ");
                        }
                    } else {
                        sbFullPinyin.append(aToken.target);
                        sbFullPinyin.append(" ");
                    }
                }
            }
        }
        if (TextUtils.isEmpty(sbFullPinyin)) {
            return source.toLowerCase(Locale.ENGLISH);
        }
        return sbFullPinyin.toString().toLowerCase(Locale.ENGLISH);
    }

    public static String getPolyphonePinyin(String words) {
        return mPolyPhoneWords.get(words);
    }

    public static char getFirstLetterByName(final String name) {
        final String pinYin = getPinyinByName(name);
        return getFirstLetterFromPinyin(pinYin);
    }

    private static char getFirstLetterFromPinyin(final String pinyin) {
        char rv = '#';
        if (!TextUtils.isEmpty(pinyin)) {
            final char firstLetter = pinyin.toUpperCase().charAt(0);
            if ((firstLetter >= 'A') && (firstLetter <= 'Z')) {
                rv = firstLetter;
            }
        }
        return rv;
    }


}
