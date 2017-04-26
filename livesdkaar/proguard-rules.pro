#1.基本指令区
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-ignorewarning
-printmapping proguardMapping.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

#2.默认保留区
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.v8.renderscript.** {*;}
-keep class android.support.** {*;}

#eventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.** { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#milink
-keep public class com.mi.milink.sdk.connection.** { *; }

#greenDao
-keep class de.greenrobot.dao.** {*;}
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties

#rx
-dontwarn sun.misc.**
-keep class rx.** {*;}
-keep class com.trello.rxlifecycle.** {*;}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

#proto
-keep public class com.wali.live.proto.** { *; }

#galileo
-keep class com.xiaomi.conferencemanager.**{ *; }
-keep class com.xiaomi.broadcaster.**{ *; }
-keep class org.webrtc.**{ *; }
-keep class org.xplatform_util.**{ *;}
-keep class com.xiaomi.devicemanager.**{ *; }
-keep class com.xiaomi.rendermanager.**{ *; }
-keep class com.xiaomi.player.** { *; }

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
-keep public class ch.qos.logback.** { *; }
-keep class org.slf4j.** { *; }

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

-keepclasseswithmembernames class * {
    native <methods>;
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
-keep class com.tencent.** {*;}
-keep class com.wali.gamecenter.report.** {*;}
-keep class com.xiaomi.gamecenter.** {*;}
-dontwarn com.taobao.**
-dontwarn com.alibaba.**
-dontwarn com.alipay.**
-keep class com.ut.** {*;}
-dontwarn com.ut.**
-keep class com.ta.** {*;}
-keep class mtopsdk.** {*;}
-dontwarn com.ta.**
-keep class org.json.** {*;}
-keep class com.ali.auth.** {*;}

-keep class com.wali.live.watchsdk.IMiLiveSdk{
    public *;
}

-keep class com.wali.live.livesdk.live.MiLiveSdkController{
    public *;
}

#数据保留区
-keep class com.wali.live.watchsdk.ipc.service.LiveInfo {*;}
-keep class com.wali.live.watchsdk.ipc.service.UserInfo {*;}