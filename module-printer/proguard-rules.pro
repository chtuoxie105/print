# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.paycoo.outprintdevice.printutil.SendCallback { *; }
-dontwarn class com.starmicronics.starioextension.** {*;}
-dontwarn class com.starmicronics.stario.** {*;}
# 代码混淆压缩比，在0~7之间，默认为5，
-dontwarn com.android.dualscreenmanager.**
-optimizationpasses 5
# 混淆时不使用大小写混合，混淆后的类名为dualscreenmanager小写
-dontusemixedcaseclassnames
# 指定不去忽略非公共的库的类
-dontskipnonpubliclibraryclasses
# 指定不去忽略非公共的库的类的成员
-dontskipnonpubliclibraryclassmembers
# 不做预校验，可加快混淆速度
# preverify是proguard的4个步骤之一
# Android不需要preverify，去掉这一步可以加快混淆速度
-dontpreverify
# 混淆时生成日志文件，即映射文件
-verbose
# 不优化输入的类文件
-dontoptimize
# 指定映射文件的名称
-printmapping proguardMapping.txt
#混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
# 保护代码中的Annotation不被混淆
-keepattributes *Annotation*
# 忽略警告
-ignorewarnings
# 保护泛型不被混淆
-keepattributes Signature
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
#same with-ignorewarnings
-dontwarn

#-----------需要保留的东西--------------
# 保留所有的本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
# support-v4
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
# support-v7
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.app.** { *; }
-keep public class * extends android.support.v7.**
-keep class android.support.design.**
-keep class android.support.annotations.**

-keepclasseswithmembernames class * {
native <methods>;
}

-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 不混淆资源类
-keepclassmembers class **.R$* { *; }
-keep class **.R
-keep class **.R$* {    <fields>;}

-libraryjars libs/starioextension.jar
-libraryjars libs/StarIOPort3.1.jar
