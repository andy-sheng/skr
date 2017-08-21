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

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class com.mi.liveassistant.player.VideoPlayerWrapperView{
    public *;
}

-keep class com.mi.liveassistant.player.VideoPlayerWrapperView$* {
    *;
}

-keep class org.webrtc.**{ *; }
-keep class org.xplatform_util.**{ *;}
-keep class com.xiaomi.devicemanager.**{ *; }
-keep class com.xiaomi.rendermanager.**{ *; }
-keep class com.xiaomi.player.** { *; }