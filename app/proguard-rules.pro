# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/ramesh/Ramesh/Softwares/android-sdk-linux/tools/proguard/proguard-android.txt
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
-keepattributes Signature

-dontwarn com.squareup.okhttp.internal.huc.**
-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn org.codehaus.mojo.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

-dontwarn butterknife.internal.**
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn com.google.common.collect.*
-dontwarn com.google.common.util.concurrent**
-dontwarn com.google.common.io.BaseEncoding
-dontwarn com.aphidmobile.flip.Card
-dontwarn dagger.internal.codegen.*
-dontwarn com.google.common.io.*
-dontwarn com.google.common.eventbus.*

# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models. Modify to fit the structure
# of your app.
-keepclassmembers class com.yo.android.model.** {
  *;
}