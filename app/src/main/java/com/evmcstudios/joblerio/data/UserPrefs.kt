package com.evmcstudios.joblerio.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object UserPrefs {

    private const val PREFS_NAME = "user_prefs"
    private const val INSTALL_PREFS_NAME = "install_prefs"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_AVATAR_URL = "user_avatar_url"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_LAST_VERSION_CODE = "last_version_code"
    private const val KEY_LAST_UPDATE_TIME = "last_update_time"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getInstallPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(INSTALL_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(context: Context, name: String, email: String, avatarUrl: String = "") {
        getPrefs(context).edit()
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_AVATAR_URL, avatarUrl)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    fun getUserName(context: Context): String {
        return getPrefs(context).getString(KEY_NAME, "") ?: ""
    }

    fun getUserEmail(context: Context): String {
        return getPrefs(context).getString(KEY_EMAIL, "") ?: ""
    }

    fun getAvatarUrl(context: Context): String {
        return getPrefs(context).getString(KEY_AVATAR_URL, "") ?: ""
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LOGGED_IN, false)
    }

    fun clear(context: Context) {
        getPrefs(context).edit().clear().commit()
    }

    fun checkAndClearOnVersionUpgrade(context: Context, currentVersionCode: Int): Boolean {
        val installPrefs = getInstallPrefs(context)
        val lastVersion = installPrefs.getInt(KEY_LAST_VERSION_CODE, -1)

        Log.d("UserPrefs", "Version check: stored=$lastVersion, current=$currentVersionCode")

        if (lastVersion != -1 && lastVersion != currentVersionCode) {
            Log.d("UserPrefs", "Version upgrade detected: $lastVersion -> $currentVersionCode, clearing stale data")
            installPrefs.edit().putInt(KEY_LAST_VERSION_CODE, currentVersionCode).commit()
            clear(context)
            clearAllSharedPreferences(context)
            return true
        }

        installPrefs.edit().putInt(KEY_LAST_VERSION_CODE, currentVersionCode).commit()
        return false
    }

    fun checkAndClearOnReinstall(context: Context, lastUpdateTime: Long, firstInstallTime: Long): Boolean {
        val now = System.currentTimeMillis()
        val freshInstall = (now - firstInstallTime) < 5 * 60 * 1000

        Log.d("UserPrefs", "Reinstall check: firstInstall=$firstInstallTime, now=$now, freshInstall=$freshInstall")

        if (freshInstall) {
            val installPrefs = getInstallPrefs(context)
            val storedUpdateTime = installPrefs.getLong(KEY_LAST_UPDATE_TIME, -1L)

            if (storedUpdateTime != -1L) {
                Log.d("UserPrefs", "Fresh install with stale backup data (stored=$storedUpdateTime, current=$lastUpdateTime), clearing")
            } else {
                Log.d("UserPrefs", "First ever install, clearing any restored data")
            }

            installPrefs.edit().putLong(KEY_LAST_UPDATE_TIME, lastUpdateTime).commit()
            clear(context)
            clearAllSharedPreferences(context)
            return true
        }

        val installPrefs = getInstallPrefs(context)
        val storedUpdateTime = installPrefs.getLong(KEY_LAST_UPDATE_TIME, -1L)

        if (storedUpdateTime != -1L && storedUpdateTime != lastUpdateTime) {
            Log.d("UserPrefs", "Update detected: stored=$storedUpdateTime, current=$lastUpdateTime, clearing")
            installPrefs.edit().putLong(KEY_LAST_UPDATE_TIME, lastUpdateTime).commit()
            clear(context)
            clearAllSharedPreferences(context)
            return true
        }

        installPrefs.edit().putLong(KEY_LAST_UPDATE_TIME, lastUpdateTime).commit()
        return false
    }

    private fun clearAllSharedPreferences(context: Context) {
        val prefsNames = listOf(
            PREFS_NAME,
            "home_state",
            "reengagement",
            "viewed_jobs",
            "job_alerts",
            "recent_searches",
            "filter_prefs",
            "referrer_prefs",
            "postback_prefs",
            "resumes",
            "saved_jobs"
        )
        for (name in prefsNames) {
            context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
        }
    }
}
