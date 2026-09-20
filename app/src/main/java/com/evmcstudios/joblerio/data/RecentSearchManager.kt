package com.evmcstudios.joblerio.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class RecentSearch(
    val query: String,
    val location: String,
    val timestamp: Long = System.currentTimeMillis()
)

object RecentSearchManager {

    private const val PREFS_NAME = "recent_searches"
    private const val KEY_SEARCHES = "searches_list"
    private const val MAX_RECENT = 20

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun addSearch(context: Context, query: String, location: String) {
        if (query.isBlank()) return
        val searches = getSearches(context).toMutableList()
        searches.removeAll { it.query.equals(query, ignoreCase = true) && it.location.equals(location, ignoreCase = true) }
        searches.add(0, RecentSearch(query, location))
        if (searches.size > MAX_RECENT) {
            searches.subList(MAX_RECENT, searches.size).clear()
        }
        val json = Gson().toJson(searches)
        getPrefs(context).edit().putString(KEY_SEARCHES, json).apply()
    }

    fun getSearches(context: Context): List<RecentSearch> {
        val json = getPrefs(context).getString(KEY_SEARCHES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<RecentSearch>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearSearches(context: Context) {
        getPrefs(context).edit().remove(KEY_SEARCHES).apply()
    }
}
