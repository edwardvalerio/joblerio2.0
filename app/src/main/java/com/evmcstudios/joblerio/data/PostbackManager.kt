package com.evmcstudios.joblerio.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object PostbackManager {

    private const val TAG = "PostbackManager"
    private const val PREFS_NAME = "postback_prefs"
    private const val KEY_POSTBACK_FIRED = "postback_fired"
    private const val KEY_JOB_CLICK_COUNT = "job_click_count"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isPostbackFired(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_POSTBACK_FIRED, false)
    }

    fun getJobClickCount(context: Context): Int {
        return getPrefs(context).getInt(KEY_JOB_CLICK_COUNT, 0)
    }

    fun incrementJobClickCount(context: Context): Int {
        val current = getJobClickCount(context)
        val newCount = current + 1
        getPrefs(context).edit().putInt(KEY_JOB_CLICK_COUNT, newCount).apply()
        Log.d(TAG, "Job click count: $newCount")
        return newCount
    }

    suspend fun checkAndFirePostback(context: Context) {
        if (isPostbackFired(context)) return

        val eventType = RemoteConfigManager.getPostbackEvent()
        val clickId = ReferrerManager.getClickId(context)

        if (clickId.isBlank()) {
            Log.d(TAG, "No click ID, skipping postback")
            return
        }

        when (eventType) {
            "install" -> {
                firePostback(context, clickId)
            }
            "down_funnel" -> {
                val threshold = RemoteConfigManager.getPostbackClickThreshold()
                val clickCount = getJobClickCount(context)
                if (clickCount >= threshold) {
                    firePostback(context, clickId)
                } else {
                    Log.d(TAG, "Down funnel: $clickCount/$threshold clicks, waiting")
                }
            }
        }
    }

    private suspend fun firePostback(context: Context, clickId: String) {
        if (isPostbackFired(context)) return

        val baseUrl = RemoteConfigManager.getPostbackUrl()
        val key = RemoteConfigManager.getPostbackKey()
        val payout = RemoteConfigManager.getPayout()

        val url = "$baseUrl?clickid=${URLEncoder.encode(clickId, "UTF-8")}" +
            "&payout=${URLEncoder.encode(payout, "UTF-8")}" +
            "&key=${URLEncoder.encode(key, "UTF-8")}"

        Log.d(TAG, "Firing postback: $url")

        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Joblerio/1.0")
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                val code = response.code
                Log.d(TAG, "Postback response: $code")

                if (code in 200..299) {
                    getPrefs(context).edit().putBoolean(KEY_POSTBACK_FIRED, true).apply()
                    Log.d(TAG, "Postback fired successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Postback failed: ${e.message}")
            }
        }
    }

    fun reset(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
