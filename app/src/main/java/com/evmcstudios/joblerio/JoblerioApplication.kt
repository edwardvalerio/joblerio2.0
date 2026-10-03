package com.evmcstudios.joblerio

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class JoblerioApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            Log.d("JoblerioApplication", "Firebase initialized")
        } catch (e: Exception) {
            Log.e("JoblerioApplication", "Firebase init failed: ${e.message}")
        }
    }

    companion object {
        @Volatile
        private var analytics: FirebaseAnalytics? = null

        fun analyticsOrNull(): FirebaseAnalytics? {
            if (analytics != null) return analytics
            return try {
                analytics = Firebase.analytics
                analytics
            } catch (e: Exception) {
                Log.e("JoblerioApplication", "Analytics unavailable: ${e.message}")
                null
            }
        }
    }
}
