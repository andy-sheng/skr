package com.wali.live.common.smiley;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.live.module.common.R;
import com.base.log.MyLog;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @module smiley
 * <p>
 * Created by MK on 15/9/24.
 */
public class SmileyParser {
    // Singleton stuff
    private static SmileyParser sInstance;

    private static final float SMILEY_SIZE = 1.25f;

    private static final int MAX_SMILEY_PER_TEXT = 250;

    public synchronized void init(Context context) {

        try {
            mSmileyV6Texts = context.getResources().getStringArray(R.array.smiley_v6_texts);
            mSmileyNormalTexts = context.getResources().getStringArray(R.array.smiley_normal_texts);
            mSmileyNatureTexts = context.getResources().getStringArray(R.array.smiley_nature_texts);
            mSmileySignTexts = context.getResources().getStringArray(R.array.smiley_sign_texts);
            mSmileyLifeTexts = context.getResources().getStringArray(R.array.smiley_life_texts);
            mSmileyCarTexts = context.getResources().getStringArray(R.array.smiley_car_texts);

            mSmileyV6TextsGlobal = context.getResources().getStringArray(
                    R.array.smiley_v6_texts_global);
            mSmileyNormalTextsGlobal = context.getResources().getStringArray(
                    R.array.smiley_normal_texts_global);
            mSmileyNatureTextsGlobal = context.getResources().getIntArray(
                    R.array.smiley_nature_texts_global);
            mSmileySignTextsGlobal = context.getResources().getIntArray(
                    R.array.smiley_sign_texts_global);
            mSmileyLifeTextsGlobal = context.getResources().getIntArray(
                    R.array.smiley_life_texts_global);
            mSmileyCarTextsGlobal = context.getResources().getIntArray(R.array.smiley_car_texts_global);

            buildSmileyToRes();
            buildPattern();
        } catch (Exception e) {
            MyLog.e(e);
        }

    }

    public static synchronized SmileyParser getInstance() {
        if (sInstance == null) {
            sInstance = new SmileyParser();
            sInstance.init(GlobalData.app());
        }
        return sInstance;
    }

    public static void destroy() {
        sInstance = null;
    }

    public static void setText(final TextView textView, final CharSequence text) {
        setText(textView, text, true);
    }

    public static void setText(final TextView textView, final CharSequence text,
                               boolean synchnorizedParse) {
        final CharSequence newText =
                SmileyParser.getInstance().addSmileySpans(textView.getContext(), (text != null) ?
                        text : "", textView.getTextSize(), synchnorizedParse);
        textView.setText(newText);
    }

    /**
     * 本地用来将string变成表情串的pattern
     */
    private static Pattern mNativePattern;

    /**
     * 网络收到的unicode串到本地可识别表情标示的pattern
     */
    private static Pattern mGlobalToLocalPattern;

    /**
     * 本地可识别的表情标示到unicode的pattern
     */
    private static Pattern mLocalToGlobalPattern;

    // 四种表情对应的本地的字符数组
    public static String[] mSmileyV6Texts, mSmileyNormalTexts, mSmileyNatureTexts, mSmileySignTexts,
            mSmileyLifeTexts, mSmileyCarTexts;

    // 四种对应的Global(英文)字符数组
    // 　一定要注意第一个,因为加了新表情,导致数组类型变为String,下面在处理的地方有差异,切记
    public static String[] mSmileyV6TextsGlobal, mSmileyNormalTextsGlobal;
    public static int[] mSmileyNatureTextsGlobal, mSmileySignTextsGlobal, mSmileyLifeTextsGlobal,
            mSmileyCarTextsGlobal;


    // 从Text到ResId,主要是在字符串Parse成图片时候用
    private static final HashMap<String, Integer> mSmileyTextToRes = new HashMap<String, Integer>();

    /**
     * 两个转码HashMap，一个是本地到网络共用的，一个是网络到本地的
     */
    private static HashMap<String, String> mLocalToGlobalMap = new HashMap<String, String>();

    private static HashMap<String, String> mGlobalToLocalMap = new HashMap<String, String>();

    private static LruCache<Integer, Bitmap> mSmileyCacheForRes = new LruCache<Integer, Bitmap>(24);

    // 转换类型，是从本地到网络或相反
    public static final int TYPE_LOCAL_TO_GLOBAL = 1;
    public static final int TYPE_GLOBAL_TO_LOCAL = 2;
    public static final int[] mSmileyV6Ids = {
            R.drawable.mm001,
            R.drawable.mm002,
            R.drawable.mm003,
            R.drawable.mm004,
            R.drawable.mm005,
            R.drawable.mm006,
            R.drawable.mm007,
            R.drawable.mm008,
            R.drawable.mm009,
            R.drawable.mm010,
            R.drawable.mm011,
            R.drawable.mm012,
            R.drawable.mm013,
            R.drawable.mm014,
            R.drawable.mm015,
            R.drawable.mm016,
            R.drawable.mm017,
            R.drawable.mm018,
            R.drawable.mm019,
            R.drawable.mm020,
            R.drawable.mm021,
            R.drawable.mm022,
            R.drawable.mm023,
            R.drawable.mm024,
            R.drawable.mm025,
            R.drawable.mm026,
            R.drawable.mm027,
            R.drawable.mm028,
            R.drawable.mm029,
            R.drawable.mm030,
            R.drawable.mm031,
            R.drawable.mm032,
            R.drawable.mm033,
            R.drawable.mm034,
            R.drawable.mm035,
            R.drawable.mm036,
            R.drawable.mm037,
            R.drawable.mm038,
            R.drawable.mm039,
            R.drawable.mm040,
            R.drawable.mm041,
            R.drawable.mm042,
            R.drawable.mm043,
            R.drawable.mm044,
            R.drawable.mm045,
            R.drawable.mm046,
            R.drawable.mm047,
            R.drawable.mm048,
            R.drawable.mm049,
            R.drawable.mm050,
            R.drawable.mm051,
            R.drawable.mm052,
            R.drawable.mm053,
            R.drawable.mm054,
            R.drawable.mm055,
            R.drawable.mm056,
            R.drawable.mm057,
            R.drawable.mm058,
            R.drawable.mm059,
            R.drawable.mm060,
            R.drawable.mm061,
            R.drawable.mm062,

            R.drawable.e1,
            R.drawable.e2,
            R.drawable.e3,
            R.drawable.e4,
            R.drawable.e5,
            R.drawable.e6,
            R.drawable.e7,
            R.drawable.e8,
            R.drawable.e9,
            R.drawable.e10,
            R.drawable.e11,
            R.drawable.e12,
            R.drawable.e13,
            R.drawable.e14,
            R.drawable.e15,
            R.drawable.e16,
            R.drawable.e17,
            R.drawable.e18,
            R.drawable.e19,
            R.drawable.e20,
            R.drawable.e21,
            R.drawable.e22,
            R.drawable.e23,
            R.drawable.e24,
            R.drawable.e25,
            R.drawable.e26,
            R.drawable.e27,
            R.drawable.e28,
            R.drawable.e29,
            R.drawable.e30,
            R.drawable.e31,
            R.drawable.e32,
            R.drawable.e33,
            R.drawable.e34,
            R.drawable.e35,
            R.drawable.e36,
            R.drawable.e37,
            R.drawable.e38,
            R.drawable.e39,
            R.drawable.e40,
            R.drawable.e41,
            R.drawable.e42,
            R.drawable.e43,
            R.drawable.e44,
    };

    // 四种表情对应的内置资源图片的DrawablesIds
    public static final int[] mSmileyNormalDwIds = {
            R.drawable.e04f,
            R.drawable.e524,
            R.drawable.e52c,
            R.drawable.e52a,
            R.drawable.e52f,
            R.drawable.e52d,
            R.drawable.e055,
            R.drawable.e520,
            R.drawable.e43e,
            R.drawable.e03e,
            R.drawable.e303,
            R.drawable.e12a,
            R.drawable.e145,
            R.drawable.e42b,
            R.drawable.e035,
            R.drawable.e33f,
            R.drawable.e132,
            R.drawable.m024,
            R.drawable.m025,
            R.drawable.e417,
            R.drawable.e40a,
            R.drawable.e409,
            R.drawable.e403,
            R.drawable.e401,
            R.drawable.e406,
            R.drawable.e412,
            R.drawable.m042,
            R.drawable.m045,
            R.drawable.e32c,
            R.drawable.e32a,
            R.drawable.e32d,
            R.drawable.e328,
            R.drawable.e32b,
            R.drawable.e327,
            R.drawable.e334,
            R.drawable.e330,
            R.drawable.e326,
            R.drawable.e010,
            R.drawable.e41e,
            R.drawable.e012,
            R.drawable.e422,
            R.drawable.e427,
            R.drawable.e41d,
            R.drawable.e00f,
            R.drawable.e41f,
            R.drawable.e201,
            R.drawable.e115,
            R.drawable.e428,
            R.drawable.e51f,
            R.drawable.e429,
            R.drawable.e424,
            R.drawable.e423,
            R.drawable.e253,
            R.drawable.e426,
            R.drawable.e31e,
            R.drawable.e31f,
            R.drawable.e31d,
            R.drawable.e51a,
            R.drawable.e519,
            R.drawable.e518,
            R.drawable.e515,
            R.drawable.e516,
            R.drawable.e517,
            R.drawable.e51b,
            R.drawable.e152,
            R.drawable.e51c,
            R.drawable.e51e,
            R.drawable.e536,
            R.drawable.e003,
            R.drawable.e41b,
            R.drawable.e419,
            R.drawable.e41a,
    },
            mSmileyNatureDwIds = {
                    R.drawable.e443,
                    R.drawable.e528,
                    R.drawable.e134,
                    R.drawable.e530,
                    R.drawable.e529,
                    R.drawable.e526,
                    R.drawable.e523,
                    R.drawable.e019,
                    R.drawable.e306,
                    R.drawable.e030,
                    R.drawable.e304,
                    R.drawable.e110,
                    R.drawable.e305,
                    R.drawable.e118,
                    R.drawable.e447,
                    R.drawable.e119,
                    R.drawable.e444,
                    R.drawable.e441,
            },
            mSmileyLifeDwIds = {
                    R.drawable.e436,
                    R.drawable.e438,
                    R.drawable.e43a,
                    R.drawable.e439,
                    R.drawable.e43b,
                    R.drawable.e117,
                    R.drawable.e440,
                    R.drawable.e442,
                    R.drawable.e446,
                    R.drawable.e127,
                    R.drawable.e00a,
                    R.drawable.e00b,
                    R.drawable.e316,
                    R.drawable.e129,
                    R.drawable.e141,
                    R.drawable.e142,
                    R.drawable.e317,
                    R.drawable.e128,
                    R.drawable.e14b,
                    R.drawable.e211,
                    R.drawable.e114,
                    R.drawable.e313,
                    R.drawable.e116,
                    R.drawable.e104,
                    R.drawable.e103,
                    R.drawable.e102,
                    R.drawable.e140,
                    R.drawable.e11f,
                    R.drawable.e031,
                    R.drawable.e016,
                    R.drawable.e015,
                    R.drawable.e014,
                    R.drawable.e42c,
                    R.drawable.e42d,
                    R.drawable.e017,
                    R.drawable.e013,
                    R.drawable.e20e,
                    R.drawable.e20c,
                    R.drawable.e20f,
                    R.drawable.e20d,
                    R.drawable.e130,
                    R.drawable.e324,
                    R.drawable.e301,
                    R.drawable.e148,
                    R.drawable.e502,
                    R.drawable.e30a,
                    R.drawable.e042,
                    R.drawable.e040,
                    R.drawable.e12c,
                    R.drawable.e007,
                    R.drawable.e31a,
                    R.drawable.e31b,
                    R.drawable.e006,
                    R.drawable.e302,
                    R.drawable.e319,
                    R.drawable.e321,
                    R.drawable.e314,
                    R.drawable.e503,
                    R.drawable.e318,
                    R.drawable.e11e,
                    R.drawable.e338,
                    R.drawable.e30b,
                    R.drawable.e043,
                    R.drawable.e341,
                    R.drawable.e34c,
                    R.drawable.e342,
                    R.drawable.e33d,
                    R.drawable.e33e,
                    R.drawable.e34d,
                    R.drawable.e339,
                    R.drawable.e147,
                    R.drawable.e343,
                    R.drawable.e33c,
                    R.drawable.e43f,
                    R.drawable.e046,
                    R.drawable.e346,
                    R.drawable.e348,
                    R.drawable.e347,
                    R.drawable.e34a,
                    R.drawable.e349,
            },
            mSmileyCarDwIds = {
                    R.drawable.e036,
                    R.drawable.e157,
                    R.drawable.e038,
                    R.drawable.e153,
                    R.drawable.e155,
                    R.drawable.e14d,
                    R.drawable.e156,
                    R.drawable.e501,
                    R.drawable.e158,
                    R.drawable.e43d,
                    R.drawable.e037,
                    R.drawable.e504,
                    R.drawable.e44a,
                    R.drawable.e146,
                    R.drawable.e50a,
                    R.drawable.e505,
                    R.drawable.e506,
                    R.drawable.e122,
                    R.drawable.e508,
                    R.drawable.e509,
                    R.drawable.e03b,
                    R.drawable.e04d,
                    R.drawable.e449,
                    R.drawable.e44b,
                    R.drawable.e51d,
                    R.drawable.e44c,
                    R.drawable.e124,
                    R.drawable.e121,
                    R.drawable.e433,
                    R.drawable.e202,
                    R.drawable.e135,
                    R.drawable.e01c,
                    R.drawable.e42e,
                    R.drawable.e01b,
                    R.drawable.e15a,
                    R.drawable.e432,
                    R.drawable.e430,
                    R.drawable.e431,
                    R.drawable.e42f,
                    R.drawable.e01e,
                    R.drawable.e039,
                    R.drawable.e01f,
                    R.drawable.e125,
                    R.drawable.e03a,
                    R.drawable.e14e,
                    R.drawable.e252,
                    R.drawable.e137,
                    R.drawable.e209,
                    R.drawable.e133,
                    R.drawable.e150,
                    R.drawable.e320,
                    R.drawable.e123,
                    R.drawable.e143,
                    R.drawable.e50b,
                    R.drawable.e514,
                    R.drawable.e50c,
                    R.drawable.e50d,
                    R.drawable.e511,
                    R.drawable.e50f,
                    R.drawable.e512,
                    R.drawable.e510,
                    R.drawable.e50e,
            },
            mSmileySignDwIds = {
                    R.drawable.e21c,
                    R.drawable.e21d,
                    R.drawable.e21e,
                    R.drawable.e21f,
                    R.drawable.e220,
                    R.drawable.e221,
                    R.drawable.e222,
                    R.drawable.e223,
                    R.drawable.e224,
                    R.drawable.e225,
                    R.drawable.e210,
                    R.drawable.e232,
                    R.drawable.e233,
                    R.drawable.e235,
                    R.drawable.e234,
                    R.drawable.e236,
                    R.drawable.e237,
                    R.drawable.e238,
                    R.drawable.e239,
                    R.drawable.e23b,
                    R.drawable.e23a,
                    R.drawable.e23d,
                    R.drawable.e23c,
                    R.drawable.e24d,
                    R.drawable.e212,
                    R.drawable.e24c,
                    R.drawable.e213,
                    R.drawable.e214,
                    R.drawable.e507,
                    R.drawable.e203,
                    R.drawable.e20b,
                    R.drawable.e22a,
                    R.drawable.e22b,
                    R.drawable.e226,
                    R.drawable.e227,
                    R.drawable.e22c,
                    R.drawable.e22d,
                    R.drawable.e215,
                    R.drawable.e216,
                    R.drawable.e217,
                    R.drawable.e218,
                    R.drawable.e228,
                    R.drawable.e151,
                    R.drawable.e13a,
                    R.drawable.e208,
                    R.drawable.e14f,
                    R.drawable.e20a,
                    R.drawable.e434,
                    R.drawable.e309,
                    R.drawable.e315,
                    R.drawable.e30d,
                    R.drawable.e207,
                    R.drawable.e229,
                    R.drawable.e206,
                    R.drawable.e205,
                    R.drawable.e204,
                    R.drawable.e12e,
                    R.drawable.e250,
                    R.drawable.e251,
                    R.drawable.e14a,
                    R.drawable.e149,
                    R.drawable.e23f,
                    R.drawable.e240,
                    R.drawable.e241,
                    R.drawable.e242,
                    R.drawable.e243,
                    R.drawable.e244,
                    R.drawable.e245,
                    R.drawable.e246,
                    R.drawable.e247,
                    R.drawable.e248,
                    R.drawable.e249,
                    R.drawable.e24a,
                    R.drawable.e24b,
                    R.drawable.e23e,
                    R.drawable.e532,
                    R.drawable.e533,
                    R.drawable.e534,
                    R.drawable.e535,
                    R.drawable.e21a,
                    R.drawable.e219,
                    R.drawable.e21b,
                    R.drawable.e02f,
                    R.drawable.e024,
                    R.drawable.e025,
                    R.drawable.e026,
                    R.drawable.e027,
                    R.drawable.e028,
                    R.drawable.e029,
                    R.drawable.e02a,
                    R.drawable.e02b,
                    R.drawable.e02c,
                    R.drawable.e02d,
                    R.drawable.e02e,
                    R.drawable.e24e,
                    R.drawable.e24f,
                    R.drawable.e537,
            };

    private SmileyParser() {
    }

    /**
     * Builds the hashtable we use for mapping the string version of a smiley (e.g. ":-)") to a resource ID for the icon
     * version.
     */
    private static void buildSmileyToRes() {

        // mSmileyTextToRes 哈希表初始化
        for (int i = 0; i < mSmileyV6Ids.length; i++) {
            mSmileyTextToRes.put(mSmileyV6Texts[i], mSmileyV6Ids[i]);
        }
        for (int i = 0; i < mSmileyNormalDwIds.length; i++) {
            mSmileyTextToRes.put(mSmileyNormalTexts[i], mSmileyNormalDwIds[i]);
        }
        for (int i = 0; i < mSmileyNatureDwIds.length; i++) {
            mSmileyTextToRes.put(mSmileyNatureTexts[i], mSmileyNatureDwIds[i]);
        }
        for (int i = 0; i < mSmileySignDwIds.length; i++) {
            mSmileyTextToRes.put(mSmileySignTexts[i], mSmileySignDwIds[i]);
        }
        for (int i = 0; i < mSmileyLifeDwIds.length; i++) {
            mSmileyTextToRes.put(mSmileyLifeTexts[i], mSmileyLifeDwIds[i]);
        }
        for (int i = 0; i < mSmileyCarDwIds.length; i++) {
            mSmileyTextToRes.put(mSmileyCarTexts[i], mSmileyCarDwIds[i]);
        }


        // （图标转码到）本地文字和Global文字，的两个HashMap
        for (int i = 0; i < mSmileyV6TextsGlobal.length; i++) {
            if (mSmileyV6TextsGlobal[i].startsWith("[")) {
                mLocalToGlobalMap.put(mSmileyV6Texts[i], mSmileyV6TextsGlobal[i]);
                mGlobalToLocalMap.put(mSmileyV6TextsGlobal[i], mSmileyV6Texts[i]);
            } else {
                String glbStr = "";
                try {
                    glbStr = String.valueOf((char) Integer.decode(
                            mSmileyV6TextsGlobal[i]).intValue());
                    mLocalToGlobalMap.put(mSmileyV6Texts[i], glbStr);
                    mGlobalToLocalMap.put(glbStr, mSmileyV6Texts[i]);
                } catch (NumberFormatException e) {
                    MyLog.e(e);
                }
            }
        }
        for (int i = 0; i < mSmileyNormalTextsGlobal.length; i++) {
            if (mSmileyNormalTextsGlobal[i].startsWith("[")) {
                mLocalToGlobalMap.put(mSmileyNormalTexts[i], mSmileyNormalTextsGlobal[i]);
                mGlobalToLocalMap.put(mSmileyNormalTextsGlobal[i], mSmileyNormalTexts[i]);
            } else {
                try {
                    final String glbStr = String.valueOf((char) Integer.decode(
                            mSmileyNormalTextsGlobal[i]).intValue());
                    mLocalToGlobalMap.put(mSmileyNormalTexts[i], glbStr);
                    mGlobalToLocalMap.put(glbStr, mSmileyNormalTexts[i]);
                } catch (NumberFormatException e) {
                    MyLog.e(e);
                }
            }
        }
        for (int i = 0; i < mSmileyNatureTextsGlobal.length; i++) {
            mLocalToGlobalMap.put(mSmileyNatureTexts[i],
                    String.valueOf((char) mSmileyNatureTextsGlobal[i]));
            mGlobalToLocalMap.put(String.valueOf((char) mSmileyNatureTextsGlobal[i]),
                    mSmileyNatureTexts[i]);
        }
        for (int i = 0; i < mSmileySignTextsGlobal.length; i++) {
            mLocalToGlobalMap.put(mSmileySignTexts[i],
                    String.valueOf((char) mSmileySignTextsGlobal[i]));
            mGlobalToLocalMap.put(String.valueOf((char) mSmileySignTextsGlobal[i]),
                    mSmileySignTexts[i]);
        }
        for (int i = 0; i < mSmileyLifeTextsGlobal.length; i++) {
            mLocalToGlobalMap.put(mSmileyLifeTexts[i],
                    String.valueOf((char) mSmileyLifeTextsGlobal[i]));
            mGlobalToLocalMap.put(String.valueOf((char) mSmileyLifeTextsGlobal[i]),
                    mSmileyLifeTexts[i]);
        }
        for (int i = 0; i < mSmileyCarTextsGlobal.length; i++) {
            mLocalToGlobalMap.put(mSmileyCarTexts[i],
                    String.valueOf((char) mSmileyCarTextsGlobal[i]));
            mGlobalToLocalMap.put(String.valueOf((char) mSmileyCarTextsGlobal[i]),
                    mSmileyCarTexts[i]);
        }
    }

    /**
     * Builds the regular expression we use to find smileys in {@link #addSmileySpans}.
     */
    private static void buildPattern() {
        // Set the StringBuilder capacity with the assumption that the average
        // smiley is 3 characters long.
        StringBuilder patternString = new StringBuilder();

        // Build a regex that looks like (:-)|:-(|...), but escaping the smilies
        // properly so they will be interpreted literally by the regex matcher.
        // And now , texts is four so all append to the last
        patternString.append('(');
        for (final String s : mSmileyV6Texts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        for (final String s : mSmileyNormalTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        for (final String s : mSmileyNatureTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        for (final String s : mSmileySignTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        for (final String s : mSmileyLifeTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        for (final String s : mSmileyCarTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        mLocalToGlobalPattern = Pattern.compile(patternString.substring(0,
                patternString.length() - 1) + ")");

        // Replace the extra '|' with a ')'
        patternString.replace(patternString.length() - 1, patternString.length(), ")");
        mNativePattern = Pattern.compile(patternString.toString());

        // -------------------GlobalStringPattern--------------
        patternString = new StringBuilder();
        // Build a regex that looks like (:-)|:-(|...), but escaping the smilies
        // properly so they will be interpreted literally by the regex matcher.
        // And now , texts is four so all append to the last
        patternString.append('(');
        for (final String s : mSmileyV6TextsGlobal) {
            if (s.startsWith("[")) {
                patternString.append(Pattern.quote(s));
                patternString.append('|');
            } else {
                patternString.append((char) Integer.decode(s).intValue());
                patternString.append('|');
            }
        }
        for (final String s : mSmileyNormalTextsGlobal) {
            if (s.startsWith("[")) {
                patternString.append(Pattern.quote(s));
                patternString.append('|');
            } else {
                patternString.append((char) Integer.decode(s).intValue());
                patternString.append('|');
            }
        }
        for (final int s : mSmileyNatureTextsGlobal) {
            patternString.append((char) s);
            patternString.append('|');
        }
        for (final int s : mSmileySignTextsGlobal) {
            patternString.append((char) s);
            patternString.append('|');
        }
        for (final int s : mSmileyLifeTextsGlobal) {
            patternString.append((char) s);
            patternString.append('|');
        }
        for (final int s : mSmileyCarTextsGlobal) {
            patternString.append((char) s);
            patternString.append('|');
        }

        // Replace the extra '|' with a ')'
        patternString.replace(patternString.length() - 1, patternString.length(), ")");
        mGlobalToLocalPattern = Pattern.compile(patternString.toString());
    }

    private static final int SMILEY_IN_RES = 1;

    /**
     * 消息存储的时候emoji表情是存成了unicode格式。 TYPE_LOCAL_TO_GLOBAL 将本地生成的汉字代表的表情转换成unicode标示的表情，便于网络传输。 TYPE_GLOBAL_TO_LOCAL
     * 将unicode表情转换成汉字标示的表情。
     *
     * @param srcStr
     * @param type
     * @return
     */
    public CharSequence convertString(final CharSequence srcStr, final int type) {
        CharSequence result = null;
        if (srcStr != null) {
            if (type == TYPE_LOCAL_TO_GLOBAL) {
                result = convertString(srcStr, mLocalToGlobalPattern.matcher(srcStr), mLocalToGlobalMap);
            } else if (type == TYPE_GLOBAL_TO_LOCAL) {
                try {
                    result = convertString(srcStr, mGlobalToLocalPattern.matcher(srcStr), mGlobalToLocalMap);
                } catch (NullPointerException e) {
                    MyLog.e(e);
                }
            }
            if (result == null || result.length() == 0) {
                result = srcStr;
            }
        }
        return result;
    }

    /**
     * 从原始串中找出Mather，并替换成相应的String
     *
     * @param srcStr     原始串
     * @param matcher    匹配Mather
     * @param convertMap
     * @return
     */
    private CharSequence convertString(final CharSequence srcStr, final Matcher matcher,
                                       final HashMap<String, String> convertMap) {
        final SpannableStringBuilder convertedSB = new SpannableStringBuilder();
        int offset = 0;
        int MAX_FIND_TIMES = 500;
        int times = 0;
        try {
            while (matcher.find() && times < MAX_FIND_TIMES) {
                if (matcher.start() > offset) {
                    convertedSB.append(srcStr.subSequence(offset, matcher.start()));
                }
                final String matcherStr = matcher.group();
                if (convertMap.get(matcherStr) != null) {
                    convertedSB.append(convertMap.get(matcherStr));
                }
                offset = matcher.end();
                times++;
            }
            if (offset < srcStr.length()) {
                convertedSB.append(srcStr.subSequence(offset, srcStr.length()));
            }
        } catch (Exception e) {
            MyLog.e(e.getMessage());
        } catch (Error error) {
            MyLog.e(error.getMessage());
        }
        return convertedSB;
    }

    public CharSequence addSmileySpans(Context context, final CharSequence text,
                                       final float smallType) {
        return addSmileySpans(context, text, smallType, true, false, true);
    }

    public CharSequence addSmileySpans(Context context, final CharSequence text,
                                       final float smallType, boolean synchronizedParse) {
        return addSmileySpans(context, text, smallType, true, false, synchronizedParse);
    }

    /**
     * Adds ImageSpans to a CharSequence that replace textual emoticons such as :-) with a graphical version.
     *
     * @param text A CharSequence possibly containing emoticons
     * @return A CharSequence annotated with ImageSpans covering any recognized emoticons.
     */

    public CharSequence addSmileySpans(Context context, final CharSequence text,
                                       final float smallType, final boolean toLocal, boolean synchronizedParse) {
        return addSmileySpans(context, text, smallType, toLocal, false, synchronizedParse);
    }

    public CharSequence addSmileySpans(Context context, CharSequence text, final float smallType,
                                       final boolean toLocal,
                                       final boolean addNoSmileyLinks, boolean synchronizedParse) {

        if (toLocal) {
            text = convertString(text, SmileyParser.TYPE_GLOBAL_TO_LOCAL);
        }
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        CharSequence builder = getBuilderByImageSpanAlign(context, text, smallType,
                ImageSpan.ALIGN_BASELINE, synchronizedParse);
        // 如果全是图片并且没有排在底部，就返回null，这时重新定位
        if (builder == null) {
            builder = getBuilderByImageSpanAlign(context, text, smallType, ImageSpan.ALIGN_BOTTOM,
                    synchronizedParse);
        }
        return builder;
    }

    // 如果是需要小图片，则将大图片的尺寸*2/3，
    private CharSequence getBuilderByImageSpanAlign(final Context context, final CharSequence text,
                                                    final float emojiSize,
                                                    final int imageSpanAlign,
                                                    final boolean synchronizedParse) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(text);
        int sourceType = -1;
        int spanLen = 0;
        final Matcher matcher = mNativePattern.matcher(text);
        int spanCount = 0;
        while (matcher.find()) {
            if (spanCount >= MAX_SMILEY_PER_TEXT) {
                break;
            }
            if (mSmileyTextToRes.get(matcher.group()) != null) {
                sourceType = SMILEY_IN_RES;
            } else {
                continue;
            }

            ImageSpan imageSpan = null;
            switch (sourceType) {
                case SMILEY_IN_RES: {
                    final int resId = mSmileyTextToRes.get(matcher.group());
                    final Bitmap bmp = getCachedDrawable(resId, context, synchronizedParse);
                    if (bmp != null) {
                        imageSpan = new SmileySpan(context.getApplicationContext(), bmp,
                                imageSpanAlign);
                        final Drawable temp = imageSpan.getDrawable();
                        temp.setBounds(0, 0, (int) (emojiSize * SMILEY_SIZE), (int) (emojiSize * SMILEY_SIZE));
                    }
                }
                break;
                default:
                    break;
            }
            if (imageSpan != null) {
                builder.setSpan(imageSpan, matcher.start(), matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spanLen += matcher.end() - matcher.start();
            }
            spanCount++;
        }

        if ((spanLen != text.length())
                || ((spanLen == text.length()) && (imageSpanAlign == ImageSpan.ALIGN_BOTTOM))) {
            return builder;
        }
        // 如果全是图片并且没有排在底部，就返回null，这时重新定位
        return null;
    }

    /**
     * 最多同时decode 20个。
     *
     * @return
     */
    private static boolean isAsyncTaskAvailable() {
        return mDecodingDrawable.size() + mDecodingLocalDrawable.size() < 20;
    }

    private static void decodeFile(final int resId, Context context) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resId, options);

//            int picWith = options.outWidth;
//            int picHeight = options.outHeight;

//            InputStream inputStream = context.getResources().openRawResource(resId);
//            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(inputStream, false);
//            Bitmap bmp = decoder.decodeRegion(new Rect(2, 2, picWith - 2, picHeight - 2), new BitmapFactory.Options());

            if (bmp != null) {
                mSmileyCacheForRes.put(resId, bmp);
            }
        } catch (OutOfMemoryError error) {
            MyLog.e(error);
        }
    }

    public static Bitmap getCachedDrawable(final int resId, final Context context,
                                           boolean synchronizedParse) {
        Bitmap bmp = mSmileyCacheForRes.get(resId);
        if (bmp == null) {
            if (synchronizedParse) {
                decodeFile(resId, context);
                return mSmileyCacheForRes.get(resId);
            } else {
                if (!mDecodingDrawable.contains(resId) && isAsyncTaskAvailable()) {
                    mDecodingDrawable.add(resId);
                    Observable.create(new Observable.OnSubscribe<Object>() {
                        @Override
                        public void call(Subscriber<? super Object> subscriber) {
                            decodeFile(resId, context);
                            subscriber.onCompleted();
                        }
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Object>() {
                                @Override
                                public void onCompleted() {
                                    mDecodingDrawable.remove(resId);
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onNext(Object o) {

                                }
                            });
                }
            }
        }
        return bmp;
    }

    private static Set<Integer> mDecodingDrawable = Collections
            .synchronizedSet(new HashSet<Integer>());
    private static Set<String> mDecodingLocalDrawable = Collections
            .synchronizedSet(new HashSet<String>());


    //内部类
    public class SmileySpan extends ImageSpan {
        public SmileySpan(Context context, Bitmap bitmap, int verticalAlignment) {
            super(context, bitmap, verticalAlignment);
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            Drawable d = getDrawable();
            Rect rect = d.getBounds();
            if (fm != null) {
                Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
                // 获得文字总高度
                int fontHeight = fmPaint.bottom - fmPaint.top;
                // 获得图片总高度
                int drHeight = rect.bottom - rect.top;
                int top = drHeight / 2 - fontHeight / 4;
                int bottom = drHeight / 2 + fontHeight / 4;

                fm.ascent = -bottom;
                fm.top = -bottom;
                fm.bottom = top;
                fm.descent = top;
            }
            return rect.right;
        }

        public void draw(Canvas canvas, CharSequence text, int start, int end,
                         float x, int top, int y, int bottom, Paint paint) {
            Drawable b = getDrawable();
            Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
            canvas.save();
            int transY;
            //获得将要显示的文本高度-图片高度除2等居中位置+top(换行情况)
            transY = ((fontMetricsInt.descent - fontMetricsInt.ascent) - b.getBounds().bottom) / 2 + top;
            //偏移画布后开始绘制
            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }
    }
}
