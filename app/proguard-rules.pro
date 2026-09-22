# Enable optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# AdMob - keep only what's needed
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.google.android.gms.ads.internal.** { *; }
-keep class com.google.android.gms.ads.query.** { *; }
-keep class com.google.android.gms.ads.formats.** { *; }

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# Firebase Config
-keep class com.google.firebase.remoteconfig.** { *; }

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
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Compose
-dontwarn androidx.compose.**

# Play Services
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.internal.** { *; }
