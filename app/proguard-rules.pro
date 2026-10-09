# Joblerio release shrinker rules.
# Google Play rejected the app for release crashes (Broken Functionality).
# Keep this comprehensive: R8 full-mode (AGP 8+/9) strips aggressively and
# Play Services / Firebase / AdMob / Install Referrer rely on reflection.

# Attributes needed by Gson, Firebase, Play Services, and Compose
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations
-keepattributes *Annotation*

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
-keep class com.evmcstudios.joblerio.data.LocationResult { *; }

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
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * {
    public <init>();
}

# --- Firebase (analytics, auth, remote config, ktx extensions) ---
-keep class com.google.firebase.** { *; }
-keep class com.google.firebase.ktx.** { *; }
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.remoteconfig.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.firebase.ktx.**

# --- Google Play Services (comprehensive: tasks, auth, base, dynamic, ads, location) ---
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.auth.api.signin.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.common.api.** { *; }
-keep class com.google.android.gms.common.internal.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
-keep class com.google.android.gms.dynamic.** { *; }
-keep class com.google.android.gms.location.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.internal.** { *; }
-keep class com.google.android.gms.base.** { *; }
-keep class com.google.android.gms.clearcut.** { *; }
-keep class com.google.android.gms.flags.** { *; }
-keep class com.google.android.gms.measurement.** { *; }
-keep class com.google.android.gms.stats.** { *; }
-keep class com.google.android.gms.tagmanager.** { *; }
-dontwarn com.google.android.gms.**

# --- Install Referrer (AIDL + Play Services binder) ---
-keep class com.android.installreferrer.** { *; }
-keep class com.android.installreferrer.api.** { *; }
-dontwarn com.android.installreferrer.**

# --- UMP (User Messaging Platform / consent) ---
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# --- AdMob ---
-keep class com.google.ads.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.ads.internal.** { *; }
-keep class com.google.android.gms.ads.nativead.** { *; }

# --- OkHttp / Okio ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**

# --- Kotlin coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Coil ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- Navigation ---
-keep class androidx.navigation.** { *; }

# --- AndroidX Startup / App Startup (WorkManager, etc.) ---
-keep class androidx.startup.** { *; }
-keep class androidx.work.** { *; }

# --- Strip verbose logs in release builds ---
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
