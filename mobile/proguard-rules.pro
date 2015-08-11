# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Thompson\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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

#-injars      bin/classes
#-injars      libs
#-outjars     bin/classes-processed.jar
#-libraryjars /usr/local/java/android-sdk/platforms/android-9/android.jar

-dontpreverify
-optimizationpasses 2
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic
-keepattributes *Annotation*

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-dontwarn android.support.**

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-keep class com.android.volley.** { *; }

-keep class org.apache.commons.logging.**

-keep class io.realm.** { *; }
-dontwarn javax.**
-dontwarn io.realm.**

-keep class ng.codehaven.bambam.models.**{*;}

-keep class com.parse.*{ *; }
-dontwarn com.parse.**


-dontwarn com.squareup.okhttp.**

-keep class org.apache.commons.logging.**               { *; }
#-keep class com.amazonaws.services.sqs.QueueUrlHandler  { *; }
-keep class com.amazonaws.javax.xml.transform.sax.*     { public *; }
-keep class com.amazonaws.javax.xml.stream.**           { *; }
-keep class com.amazonaws.services.**.model.*Exception* { *; }
-keep class com.amazonaws.internal.** 					{ *; }
-keep class org.codehaus.**                             { *; }
-keep class org.joda.convert.*							{ *; }
-keepattributes Signature,*Annotation*,EnclosingMethod
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class com.amazonaws.** { *; }

-dontwarn com.amazonaws.auth.policy.conditions.S3ConditionFactory
-dontwarn org.joda.time.**
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn javax.xml.stream.events.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.apache.commons.logging.impl.**
-dontwarn org.apache.http.conn.scheme.**
-dontwarn org.apache.http.annotation.**
-dontwarn org.ietf.jgss.**
-dontwarn org.w3c.dom.bootstrap.**

-dontwarn com.fasterxml.jackson.core.JsonFactory
-dontwarn com.fasterxml.jackson.core.JsomGenerator
-dontwarn com.fasterxml.jackson.core.JsonToken
-dontwarn com.fasterxml.jackson.core.JsonParser

-keep class com.facebook.** {*;}
-keep class bolts.** {*;}
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Thompson\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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
