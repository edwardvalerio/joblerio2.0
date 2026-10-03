# Joblerio release shrinker rules.
# Keep this file conservative: Google Play rejected the app for release crashes
# after aggressive R8 full-mode / repackaging rules were introduced.

# Attributes needed by Gson, Firebase, Play Services, and Compose
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations

# --- App models serialized with Gson (reflection) ---
-keep class com.evmcstudios.joblerio.data.Job { *; }
-keep class com.evmcstudios.joblerio.data.JobSearchResult { *; }
-keep class com.evmcstudios.joblerio.data.JobAlert { *; }
-keep class com.evmcstudios.joblerio.data.ViewedJob { *; }
-keep class com.evmcstudios.joblerio.data.FilterState { *; }
-keep class com.evmcstudios.joblerio.data.Resume { *; }
-keep class com.evmcstudios.joblerio.data.PersonalInfo { *; }
-keep class com.evmcstudios.joblerio.data.Experience { *; }
-keep class com.evmcstudios.joblerio.data.Education { *; }
-keep class com.evmcstudios.joblerio.data.RecentSearch { *; }
-keep class com.evmcstudios.joblerio.data.JobCategories$Category { *; }

# --- WorkManager workers are instantiated via reflection ---
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class com.evmcstudios.joblerio.JobAlertWorker { *; }
-keep class com.evmcstudios.joblerio.ReEngagementWorker { *; }

# --- Gson / TypeToken ---
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Firebase (analytics, auth, remote config) ---
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.auth.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.auth.**

# --- Google Play Services (location, base) ---
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.**

# --- AdMob ---
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# --- OkHttp / Okio ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Kotlin coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Strip verbose logs in release builds ---
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
