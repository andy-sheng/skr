-dontpreverify  
-dontobfuscate
-dontusemixedcaseclassnames  
-dontskipnonpubliclibraryclasses


-keep public class * extends android.app.Activity
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Service
-keep public class * extends android.app.Application
-keep public class * extends android.content.ContentProvider
-keep class android.support.v8.renderscript.** {*;}
-keep class com.xiaomi.mibi.** {
 *;
 }

-keep class com.iflytek.msc.*{
 *;
}

-keep class com.renren.*{
 *;
}

-keep class org.apache.*{
 *;
}

-keep class com.xiaomi.conferencemanager.**{
    *;
}

-keep class com.xiaomi.broadcaster.**{
    *;
}

-keep class org.webrtc.**{
    *;
}

-keep class org.xplatform_util.**{
    *;
}

-keep class com.wali.live.tpl.TplMiPushMessageReceiver {
    *;
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep public class com.kenai.jbosh.**
-keep public class com.novell.sasl.client.**
-keep public class de.measite.smack.AndroidDebugger
-keep public class com.google.code.microlog4android.**
-keepclasseswithmembernames class de.measite.smack.AndroidDebugger {
    public <init>(Connection,Writer,Reader);
}
-keep public class org.**

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


-keep class * extends android.preference.Preference { 
  public <init>(android.content.Context); 
  public <init>(android.content.Context, android.util.AttributeSet); 
  public <init>(android.content.Context, android.util.AttributeSet, int); 
  public void set*(...); 
}

-keep class * extends android.widget.LinearLayout
-keep class * extends android.widget.FrameLayout

-keep public class com.xiaomi.auth.**
-keep public class com.xiaomi.network.**
-keep public class weibo4andriod.**
-keep public class com.iflytek.**
-keepclassmembers class * {
    public void openFileChooser(android.webkit.ValueCallback,java.lang.String);
    public void openFileChooser(android.webkit.ValueCallback);
    public void openFileChooser(android.webkit.ValueCallback, java.lang.String, java.lang.String);
}

-keepclasseswithmembernames class com.xiaomi.network.*{
*;
}

-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }
-keep class com.google.zxing.** { *; }

# Application classes that will be serialized/deserialized over Gson

##---------------End: proguard configuration for Gson  ----------

##-------------------baidu map sdk ---------------
-keep class com.baidu.** { *; }
-keep class vi.com.gdi.bgl.android.**{*;}

-ignorewarnings
-dontoptimize
-dontshrink
-dontskipnonpubliclibraryclasses

# jingdong kepler
-keep class com.kepler.jd.**{*;}
-dontwarn com.kepler.jd.**
-keep class com.jingdong.jdma.**{*;}
-dontwarn com.jingdong.jdma.**
-keep class com.jingdong.crash.**{*;}
-dontwarn com.jingdong.crash.**

# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**

#百度地图
-keep class com.baidu.** {*;}
-keep class vi.com.** {*;}
-dontwarn com.baidu.**

#twitter分享
-dontwarn com.squareup.okhttp.**
-dontwarn com.google.appengine.api.urlfetch.**
-dontwarn rx.**
-dontwarn retrofit.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

#阿里百川sdk
-keepattributes Signature
-keep class sun.misc.Unsafe {*;}
-keep class com.taobao.* {*;}
-keep class com.alibaba.** {*;}
-keep class com.alipay.** {*;}
-dontwarn com.taobao.**
-dontwarn com.alibaba.**
-dontwarn com.alipay.**
-keep class com.ut.** {*;}
-dontwarn com.ut.**
-keep class com.ta.** {*;}
-dontwarn com.ta.**
-keep class org.json.** {*;}
-keep class com.ali.auth.** {*;}
