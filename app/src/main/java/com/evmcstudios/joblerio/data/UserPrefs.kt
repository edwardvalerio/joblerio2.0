package com.evmcstudios.joblerio.data

import android.content.Context
import android.content.SharedPreferences

object UserPrefs {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_AVATAR_URL = "user_avatar_url"
    private const val KEY_LOGGED_IN = "logged_in"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
        getPrefs(context).edit().clear().apply()
    }
}
