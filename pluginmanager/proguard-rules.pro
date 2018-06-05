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
-keep public class * implements android.os.Parcelable
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.v8.renderscript.** {*;}
-keep class android.support.** {*;}
-keep class com.wali.live.watchsdk.ipc.service.ThirdPartLoginData {*;}
-keep class com.wali.live.watchsdk.ipc.service.LiveInfo {*;}
-keep class com.wali.live.watchsdk.ipc.service.UserInfo {*;}
-keep class com.wali.live.watchsdk.ipc.service.ShareInfo {*;}
-keep class com.wali.live.watchsdk.watch.model.** {*;}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class de.greenrobot.event.** {*;}
-keepclassmembers class ** {
    public void onEvent*(**);
}

-keep class com.wali.live.sdk.manager.IMiLiveSdk{
    public *;
}

-keep class com.wali.live.sdk.manager.MiLiveSdkController{
    public *;
}

-keep class com.wali.live.sdk.manager.global.GlobalData{
    public *;
}

