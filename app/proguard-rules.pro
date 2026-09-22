# AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.evmcstudios.joblerio.data.** { *; }
-keep class com.google.gson.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep data classes for serialization
-keep class com.evmcstudios.joblerio.data.Job { *; }
-keep class com.evmcstudios.joblerio.data.JobSearchResult { *; }
-keep class com.evmcstudios.joblerio.data.JobAlert { *; }
-keep class com.evmcstudios.joblerio.data.ViewedJob { *; }
