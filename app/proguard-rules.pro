# R8 full mode optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# Gson - keep only serialized data classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.evmcstudios.joblerio.data.Job { *; }
-keep class com.evmcstudios.joblerio.data.JobSearchResult { *; }
-keep class com.evmcstudios.joblerio.data.JobAlert { *; }
-keep class com.evmcstudios.joblerio.data.ViewedJob { *; }
-keep class com.evmcstudios.joblerio.data.FilterState { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Strip logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
