package com.evmcstudios.joblerio.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ViewedJob(
    val url: String,
    val title: String,
    val company: String,
    val location: String,
    val viewedAt: Long = System.currentTimeMillis()
)

object ViewedJobsManager {

    private const val PREFS_NAME = "viewed_jobs"
    private const val KEY_VIEWED = "viewed_list"
    private const val MAX_VIEWED = 50

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun addViewedJob(context: Context, job: ViewedJob) {
        val viewed = getViewedJobs(context).toMutableList()
        val key = "${job.title}_${job.company}"
        viewed.removeAll { "${it.title}_${it.company}" == key }
        viewed.add(0, job)
        if (viewed.size > MAX_VIEWED) {
            viewed.subList(MAX_VIEWED, viewed.size).clear()
        }
        val json = Gson().toJson(viewed)
        getPrefs(context).edit().putString(KEY_VIEWED, json).apply()
    }

    fun getViewedJobs(context: Context): List<ViewedJob> {
        val json = getPrefs(context).getString(KEY_VIEWED, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ViewedJob>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearViewedJobs(context: Context) {
        getPrefs(context).edit().remove(KEY_VIEWED).apply()
    }
}
