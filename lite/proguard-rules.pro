#1.基本指令区
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-ignorewarning
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
-keep public class com.mi.liveassistant.proto.** { *; }

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

# app
-keep class com.mi.liveassistant.init.InitManager{
    public *;
}

-keep class com.mi.liveassistant.login.GlobalManager{
    public *;
}

-keep class com.mi.liveassistant.login.callback.IAccountListener{
    *;
}

-keep class com.mi.liveassistant.login.LoginManager{
    public *;
}

-keep class com.mi.liveassistant.login.callback.ILoginCallback{
    *;
}

-keep class com.mi.liveassistant.utils.RSASignature {
    *;
}

-keep class com.mi.liveassistant.room.manager.live.GameLiveManager{
    public *;
}

-keep class com.mi.liveassistant.room.manager.live.NormalLiveManager{
    public *;
}

-keep class com.mi.liveassistant.room.manager.watch.WatchManager{
    public *;
}

-keep class com.mi.liveassistant.camera.CameraView{
    public *;
}

-keep class com.mi.liveassistant.data.model.User{
    public *;
}

-keep class com.mi.liveassistant.data.model.Viewer {
    public *;
}

-keep class com.mi.liveassistant.room.manager.live.callback.ILiveCallback{
    public *;
}

-keep class com.mi.liveassistant.room.manager.live.callback.ILiveListener{
    public *;
}

-keep class com.mi.liveassistant.room.manager.watch.callback.IWatchCallback{
    public *;
}

-keep class com.mi.liveassistant.room.manager.watch.callback.IWatchListener{
    public *;
}

-keep class com.mi.liveassistant.room.user.UserInfoManager{
    public *;
}

-keep class com.mi.liveassistant.room.user.callback.IUserCallback{
    public *;
}

-keep class com.mi.liveassistant.room.viewer.ViewerInfoManager{
    public *;
}

-keep class com.mi.liveassistant.room.viewer.callback.IViewerCallback{
    *;
}

-keep class com.mi.liveassistant.room.viewer.callback.IViewerListener{
    *;
}

-keep class com.mi.liveassistant.avatar.AvatarUtils{
    public *;
}

-keep interface com.mi.liveassistant.unity.** {
    <methods>;
}

-keep class com.mi.liveassistant.unity.UnitySdk{
    public *;
}

-keep class com.mi.liveassistant.unity.WatchForUnity{
    public *;
}

-keep class com.mi.liveassistant.unity.LiveForUnity{
    public *;
}

-keep class com.mi.liveassistant.unity.MiLiveActivity{
    public *;
}

-keep class com.mi.liveassistant.barrage.callback.ChatMsgCallBack{
    *;
}

-keep class com.mi.liveassistant.barrage.callback.SysMsgCallBack{
    *;
}

-keep class com.mi.liveassistant.barrage.facade.MessageFacade{
    public *;
}

-keep class com.mi.liveassistant.barrage.data.** {
    *;
}
