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
-keep class com.wali.live.watchsdk.ipc.service.ThirdPartLoginData {*;}
-keep class com.wali.live.watchsdk.ipc.service.LiveInfo {*;}
-keep class com.wali.live.watchsdk.ipc.service.UserInfo {*;}

#log
-keep public class ch.qos.logback.** { *; }

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

-keep class com.xiaomi.account.** {*;}

#proto
-keep public class com.wali.live.proto.** { *; }

#greenDao
-keep class de.greenrobot.dao.** {*;}
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties

#galileo
-keep class com.xiaomi.conferencemanager.**{ *; }
-keep class com.xiaomi.broadcaster.**{ *; }
-keep class org.webrtc.**{ *; }
-keep class org.xplatform_util.**{ *;}
-keep class com.xiaomi.devicemanager.**{ *; }
-keep class com.xiaomi.rendermanager.**{ *; }
-keep class com.xiaomi.player.** { *; }

#fresco
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}