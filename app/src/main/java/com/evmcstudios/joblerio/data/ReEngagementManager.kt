package com.evmcstudios.joblerio.data

import android.content.Context
import android.content.SharedPreferences

object ReEngagementManager {

    private const val PREFS_NAME = "reengagement"
    private const val KEY_LAST_ACTIVE = "last_active"
    private const val KEY_LAST_NOTIFICATION_BRACKET = "last_bracket"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun updateLastActive(context: Context) {
        getPrefs(context).edit().putLong(KEY_LAST_ACTIVE, System.currentTimeMillis()).apply()
    }

    fun getLastActive(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
    }

    fun getTimeSinceLastActive(context: Context): Long {
        return System.currentTimeMillis() - getLastActive(context)
    }

    fun getLastNotificationBracket(context: Context): String {
        return getPrefs(context).getString(KEY_LAST_NOTIFICATION_BRACKET, "") ?: ""
    }

    fun setLastNotificationBracket(context: Context, bracket: String) {
        getPrefs(context).edit().putString(KEY_LAST_NOTIFICATION_BRACKET, bracket).apply()
    }

    fun getElapsedHours(context: Context): Int {
        return (getTimeSinceLastActive(context) / (1000 * 60 * 60)).toInt()
    }

    fun getElapsedDays(context: Context): Int {
        return (getTimeSinceLastActive(context) / (1000 * 60 * 60 * 24)).toInt()
    }

    fun shouldNotify(context: Context, enabled: Boolean): Boolean {
        if (!enabled) return false
        val elapsedHours = getElapsedHours(context)
        val elapsedDays = getElapsedDays(context)
        val lastBracket = getLastNotificationBracket(context)

        return when {
            elapsedDays >= 7 && lastBracket != "7d" -> true
            elapsedDays >= 3 && lastBracket !in listOf("3d", "7d") -> true
            elapsedDays >= 1 && lastBracket !in listOf("1d", "3d", "7d") -> true
            elapsedHours >= 3 && lastBracket !in listOf("3h", "1d", "3d", "7d") -> true
            elapsedHours >= 1 && lastBracket !in listOf("1h", "3h", "1d", "3d", "7d") -> true
            else -> false
        }
    }

    fun getCurrentBracket(context: Context): String {
        val elapsedHours = getElapsedHours(context)
        val elapsedDays = getElapsedDays(context)
        return when {
            elapsedDays >= 7 -> "7d"
            elapsedDays >= 3 -> "3d"
            elapsedDays >= 1 -> "1d"
            elapsedHours >= 3 -> "3h"
            elapsedHours >= 1 -> "1h"
            else -> ""
        }
    }
}
