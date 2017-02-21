# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/chenyong/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-libraryjars <java.home>/lib/rt.jar

-optimizationpasses 5
-dontpreverify
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#-printmapping libs/mapping.txt
-dontwarn

-keepattributes Signature
-keepattributes *Annotation*

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class * extends android.app.Activity
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Service
-keep public class * extends android.app.Application
-keep public class * extends android.content.ContentProvider