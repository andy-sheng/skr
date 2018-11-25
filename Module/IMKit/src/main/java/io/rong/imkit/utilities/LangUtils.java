//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.LocaleList;

import java.util.Locale;

import io.rong.imlib.RongIMClient.PushLanguage;

public class LangUtils {
  private static final String LOCALE_CONF_FILE_NAME = "locale.config";
  private static final String APP_LOCALE = "app_locale";
  private static final String APP_PUSH_LANGUAGE = "app_push_language";
  private static Locale systemLocale = Locale.getDefault();

  public LangUtils() {
  }

  public static Context getConfigurationContext(Context context) {
    Resources resources = context.getResources();
    Configuration config = new Configuration(resources.getConfiguration());
    Context configurationContext = context;
    if (VERSION.SDK_INT >= 24) {
      LocaleList localeList = new LocaleList(new Locale[]{getAppLocale(context).toLocale()});
      LocaleList.setDefault(localeList);
      config.setLocales(localeList);
      configurationContext = context.createConfigurationContext(config);
    }

    return configurationContext;
  }

  public static io.rong.imkit.utilities.LangUtils.RCLocale getAppLocale(Context context) {
    SharedPreferences sp = context.getSharedPreferences("locale.config", 0);
    String locale = sp.getString("app_locale", "auto");
    return io.rong.imkit.utilities.LangUtils.RCLocale.valueOf(locale);
  }

  public static void saveLocale(Context context, io.rong.imkit.utilities.LangUtils.RCLocale locale) {
    SharedPreferences sp = context.getSharedPreferences("locale.config", 0);
    sp.edit().putString("app_locale", locale.value()).commit();
  }

  public static PushLanguage getPushLanguage(Context context) {
    SharedPreferences sp = context.getSharedPreferences("locale.config", 0);
    String language = sp.getString("app_push_language", "");
    if (language.equals(PushLanguage.ZH_CN.getMsg())) {
      return PushLanguage.ZH_CN;
    } else {
      return language.equals(PushLanguage.EN_US.getMsg()) ? PushLanguage.EN_US : null;
    }
  }

  public static void setPushLanguage(Context context, PushLanguage pushLanguage) {
    SharedPreferences sp = context.getSharedPreferences("locale.config", 0);
    sp.edit().putString("app_push_language", pushLanguage.getMsg()).commit();
  }

  public static Locale getSystemLocale() {
    return systemLocale;
  }

  public static void setSystemLocale(Locale locale) {
    systemLocale = locale;
  }

  public static io.rong.imkit.utilities.LangUtils.RCLocale getCurrentLanguage(Context context) {
    SharedPreferences sp = context.getSharedPreferences("locale.config", 0);
    String locale = sp.getString("app_locale", "auto");
    if (locale.equals("auto")) {
      return getSystemLocale().toString().equals("zh_CN") ? io.rong.imkit.utilities.LangUtils.RCLocale.LOCALE_CHINA : io.rong.imkit.utilities.LangUtils.RCLocale.LOCALE_US;
    } else {
      return io.rong.imkit.utilities.LangUtils.RCLocale.valueOf(locale);
    }
  }

  public static class RCLocale {
    public static final io.rong.imkit.utilities.LangUtils.RCLocale LOCALE_CHINA = new io.rong.imkit.utilities.LangUtils.RCLocale("zh");
    public static final io.rong.imkit.utilities.LangUtils.RCLocale LOCALE_US = new io.rong.imkit.utilities.LangUtils.RCLocale("en");
    public static final io.rong.imkit.utilities.LangUtils.RCLocale LOCALE_AUTO = new io.rong.imkit.utilities.LangUtils.RCLocale("auto");
    private String rcLocale;

    private RCLocale(String rcLocale) {
      this.rcLocale = rcLocale;
    }

    public String value() {
      return this.rcLocale;
    }

    public Locale toLocale() {
      Locale locale;
      if (this.rcLocale.equals(LOCALE_CHINA.value())) {
        locale = Locale.CHINESE;
      } else if (this.rcLocale.equals(LOCALE_US.value())) {
        locale = Locale.ENGLISH;
      } else {
        locale = io.rong.imkit.utilities.LangUtils.getSystemLocale();
      }

      return locale;
    }

    public static io.rong.imkit.utilities.LangUtils.RCLocale valueOf(String rcLocale) {
      io.rong.imkit.utilities.LangUtils.RCLocale locale;
      if (rcLocale.equals(LOCALE_CHINA.value())) {
        locale = LOCALE_CHINA;
      } else if (rcLocale.equals(LOCALE_US.value())) {
        locale = LOCALE_US;
      } else {
        locale = LOCALE_AUTO;
      }

      return locale;
    }
  }
}
