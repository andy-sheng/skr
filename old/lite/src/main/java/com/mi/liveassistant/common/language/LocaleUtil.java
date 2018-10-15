package com.mi.liveassistant.common.language;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.mi.liveassistant.common.global.GlobalData;
import com.mi.liveassistant.common.preference.PreferenceUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 应用内语言相关的类<br/>
 *
 * @module 多语言、国际化
 * Created by rongzhisheng on 16-7-8.
 */
public class LocaleUtil {
    public static final String SP_LANGUAGE_FILE_NAME = "user_language_preference";
    public static final String SP_KEY_USER_SELECTED_LANGUAGE_INDEX = "user_selected_language_index";
    public static final int INDEX_FOLLOW_SYSTEM = 0;
    public static final int INDEX_SIMPLIFIED_CHINESE = 1;
    public static final int INDEX_TRADITIONAL_CHINESE = 2;
    public static final int INDEX_ENGLISH = 3;

    public static final Locale LOCALE_SIMPLIFIED_CHINESE = Locale.SIMPLIFIED_CHINESE;
    public static final Locale LOCALE_TRADITIONAL_CHINESE = Locale.TRADITIONAL_CHINESE;
    public static final Locale LOCALE_ENGLISH = Locale.US;

    private static final Map<Integer, Locale> INDEX_LOCALE_MAP;// 选择的语言的index和Locale的映射

    static {
        Map<Integer, Locale> map = new HashMap<>();
        map.put(INDEX_FOLLOW_SYSTEM, null);
        map.put(INDEX_SIMPLIFIED_CHINESE, LOCALE_SIMPLIFIED_CHINESE);
        map.put(INDEX_TRADITIONAL_CHINESE, LOCALE_TRADITIONAL_CHINESE);
        map.put(INDEX_ENGLISH, LOCALE_ENGLISH);
        INDEX_LOCALE_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * @return 不可修改的map
     */
    public static Map<Integer, Locale> getIndexLocaleMap() {
        return INDEX_LOCALE_MAP;
    }

    private static int sSelectedLanguageIndexCache = -1;

    public static void switchAppLanguage(int languageIndex) {
        sSelectedLanguageIndexCache = languageIndex;
        PreferenceUtils.setSettingInt(GlobalData.app().getSharedPreferences(SP_LANGUAGE_FILE_NAME,
                Context.MODE_PRIVATE), SP_KEY_USER_SELECTED_LANGUAGE_INDEX, languageIndex);
    }

    /**
     * 从Preference中获取用户选择的语言索引，语言索引是CHOICE_ID_MAP.keySet中的值
     *
     * @return
     */
    public static int getSelectedLanguageIndexFromPreference() {
        int index = INDEX_FOLLOW_SYSTEM;
        if (sSelectedLanguageIndexCache < 0) {
            index = PreferenceUtils.getSettingInt(GlobalData.app().getSharedPreferences(SP_LANGUAGE_FILE_NAME, Context.MODE_PRIVATE),
                    SP_KEY_USER_SELECTED_LANGUAGE_INDEX, INDEX_FOLLOW_SYSTEM);
        } else {
            index = sSelectedLanguageIndexCache;
        }
        if (!INDEX_LOCALE_MAP.keySet().contains(index)) {
            index = INDEX_FOLLOW_SYSTEM;
        }
        sSelectedLanguageIndexCache = index;
        return index;

    }

    /**
     * 从Preference中获取用户选择的语言索引，如果是跟随系统则根据系统语言做映射
     * 返回值为，1,2,3
     *
     * @return
     */
    public static int getSelectedLanguageIndex() {
        int language = LocaleUtil.getSelectedLanguageIndexFromPreference();
        if (language == LocaleUtil.INDEX_FOLLOW_SYSTEM) {
            if (LocaleUtil.getLanguageCode().equals(Locale.SIMPLIFIED_CHINESE.toString())) {
                language = LocaleUtil.INDEX_SIMPLIFIED_CHINESE;
            } else if (LocaleUtil.getLanguageCode().equals(Locale.TRADITIONAL_CHINESE)) {
                language = LocaleUtil.INDEX_TRADITIONAL_CHINESE;
            } else {
                language = LocaleUtil.INDEX_ENGLISH;
            }
        }
        return language;
    }

    /**
     * 获取应用的语言版本，考虑到了应用内可切换语言的情况<br/>
     * rongzhisheng
     *
     * @return not null
     */
    public static Locale getLocale() {
        int selectedLanguageIndex = getSelectedLanguageIndexFromPreference();
        boolean isFollowSystem = selectedLanguageIndex == INDEX_FOLLOW_SYSTEM;
        if (isFollowSystem) {
            return Locale.getDefault();
        } else {
            Locale locale = INDEX_LOCALE_MAP.get(selectedLanguageIndex);
            return locale != null ? locale : Locale.getDefault();
        }
    }

    public static boolean isChineseLocal() {
        boolean isChineseLocal;
        Locale locale = getLocale();
        if (locale == null) {
            isChineseLocal = true;
        } else {
            if (locale.equals(LOCALE_SIMPLIFIED_CHINESE) || locale.equals(LOCALE_TRADITIONAL_CHINESE)) {
                // 中文环境
                isChineseLocal = true;
            } else {
                isChineseLocal = false;
            }
        }
        return isChineseLocal;
    }


    public static boolean isEnglishLocal() {
        boolean isEnglishLocal;
        Locale locale = getLocale();
        if (locale == null) {
            isEnglishLocal = true;
        } else {
            if (locale.equals(LOCALE_ENGLISH)) {
                // 中文环境
                isEnglishLocal = true;
            } else {
                isEnglishLocal = false;
            }
        }
        return isEnglishLocal;
    }


    /**
     * 获取应用语言的字符串表示
     *
     * @return
     */
    public static String getLanguageCode() {
        return getLocale().toString();
    }

    public static void setLocale(@NonNull Locale locale) {
        Resources resources = GlobalData.app().getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public static boolean isNeedSetLocale() {
        return !GlobalData.app().getResources().getConfiguration().locale.equals(getLocale());
    }

    ///////////////////////////
    //////WebView国际化相关//////
    ///////////////////////////

    //社区公约
    public static final String GONGYUE = "http://live.mi.com/lang/%s/privacy/gongyue/gongyue.html";
    //隐私政策
    public static final String YINSI = "http://live.mi.com/lang/%s/privacy/yinsi/yinsi.html";
    //服务条款
    public static final String XIEYI = "http://live.mi.com/lang/%s/privacy/xieyi/xieyi.html";
    //联系我们
    public static final String GUANYU = "http://live.mi.com/lang/%s/privacy/guanyu/guanyu.html";
    //常见问题
    public static final String FEED_BACK_COMMON_PROBLEMS = "http://live.mi.com/lang/%s/qa/index.html";
    //主播导购订单常见问题
    public static final String SHOPPING_GUIDE_ORDER = "http://activity.zb.mi.com/shoppingguide/order.html";
    //主播导购管理常见问题
    public static final String SHOPPING_MANAGEMENT = "http://activity.zb.mi.com/shoppingguide/management.html";
    //红包说明
    public static final String RED_ENVELOPE_DESC = "http://live.mi.com/lang/%s/redpacketdesc/desc.html";

    /**
     * @return
     * @see <a href="http://wiki.n.miui.com/pages/viewpage.action?pageId=25041801">官网国际化</a>
     */
    @NonNull
    @CheckResult
    public static String getWebViewLanguage() {
        Locale locale = getLocale();
        if (LOCALE_SIMPLIFIED_CHINESE.equals(locale)) {
            return "cn";
        }
        if (LOCALE_TRADITIONAL_CHINESE.equals(locale)) {
            return "tw";
        }
        if (locale.toString().startsWith("bo")) {
            return "bo_cn";
        }
        //if (LOCALE_ENGLISH.equals(locale)) {
        //    return "en";
        //}
        return "en";
    }

    /**
     * 根据用户选择的应用语言，为WebView获取一个国际化的网址
     *
     * @param template 包含格式化字符串的网址模板，例如http://live.mi.com/lang/%s/qa/index.html，建议定义在{@link LocaleUtil}里
     * @return
     */
    @CheckResult
    public static String getWebViewUrl(@NonNull String template) {
        return String.format(template, getWebViewLanguage());
    }

}
