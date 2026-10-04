package com.evmcstudios.joblerio

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class JoblerioApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("JoblerioApp", "Uncaught exception on thread ${thread.name}: ${throwable.message}", throwable)
        }

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            Log.d("JoblerioApplication", "Firebase initialized")
        } catch (t: Throwable) {
            Log.e("JoblerioApplication", "Firebase init failed: ${t.message}", t)
        }
    }

    companion object {
        var appContext: Context? = null
            private set

        @Volatile
        private var analytics: FirebaseAnalytics? = null

        fun analyticsOrNull(): FirebaseAnalytics? {
            if (analytics != null) return analytics
            return try {
                val ctx = appContext ?: return null
                if (FirebaseApp.getApps(ctx).isNotEmpty()) {
                    analytics = Firebase.analytics
                    analytics
                } else {
                    null
                }
            } catch (t: Throwable) {
                Log.e("JoblerioApplication", "Analytics unavailable: ${t.message}", t)
                null
            }
        }
    }
}
