package com.evmcstudios.joblerio.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.URLDecoder
import kotlin.coroutines.resume

object ReferrerManager {

    private const val TAG = "ReferrerManager"
    private const val PREFS_NAME = "referrer_prefs"
    private const val KEY_CLICK_ID = "click_id"
    private const val KEY_COUNTRY = "country"
    private const val KEY_CAMPAIGN = "campaign"
    private const val KEY_KEYWORD = "keyword"
    private const val KEY_RAW_REFERRER = "raw_referrer"
    private const val KEY_REFERRER_CAPTURED = "referrer_captured"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun captureReferrer(context: Context) {
        if (getPrefs(context).getBoolean(KEY_REFERRER_CAPTURED, false)) {
            Log.d(TAG, "Referrer already captured, skipping")
            return
        }

        try {
            val client = InstallReferrerClient.newBuilder(context).build()
            val referrerUrl = suspendCancellableCoroutine<String?> { continuation ->
                var resumed = false
                client.startConnection(object : InstallReferrerStateListener {
                    override fun onInstallReferrerSetupFinished(responseCode: Int) {
                        if (resumed) return
                        resumed = true
                        when (responseCode) {
                            InstallReferrerClient.InstallReferrerResponse.OK -> {
                                try {
                                    val details = client.installReferrer
                                    continuation.resume(details.installReferrer ?: "")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to get referrer: ${e.message}")
                                    continuation.resume(null)
                                }
                            }
                            else -> {
                                Log.e(TAG, "Install referrer setup failed: $responseCode")
                                continuation.resume(null)
                            }
                        }
                        try { client.endConnection() } catch (_: Exception) {}
                    }

                    override fun onInstallReferrerServiceDisconnected() {
                        if (!resumed) {
                            resumed = true
                            continuation.resume(null)
                        }
                    }
                })
            }

            Log.d(TAG, "Raw referrer: $referrerUrl")

            if (!referrerUrl.isNullOrBlank()) {
                val params = parseReferrer(referrerUrl)
                val clickId = params["utm_source"] ?: ""
                val country = params["utm_medium"] ?: ""
                val campaign = params["utm_campaign"] ?: ""
                val keyword = params["utm_term"] ?: ""

                val countryName = CountryMapper.getCountryName(country)

                getPrefs(context).edit()
                    .putString(KEY_CLICK_ID, clickId)
                    .putString(KEY_COUNTRY, countryName)
                    .putString(KEY_CAMPAIGN, campaign)
                    .putString(KEY_KEYWORD, keyword)
                    .putString(KEY_RAW_REFERRER, referrerUrl)
                    .putBoolean(KEY_REFERRER_CAPTURED, true)
                    .apply()

                Log.d(TAG, "Referrer captured: clickId=$clickId, country=$countryName, campaign=$campaign, keyword=$keyword")
            } else {
                getPrefs(context).edit().putBoolean(KEY_REFERRER_CAPTURED, true).apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to capture referrer: ${e.message}")
        }
    }

    private fun parseReferrer(rawReferrer: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val pairs = rawReferrer.split("&")
        for (pair in pairs) {
            val kv = pair.split("=", limit = 2)
            if (kv.size == 2) {
                val key = URLDecoder.decode(kv[0], "UTF-8")
                val value = URLDecoder.decode(kv[1], "UTF-8")
                params[key] = value
            }
        }
        return params
    }

    fun getClickId(context: Context): String {
        return getPrefs(context).getString(KEY_CLICK_ID, "") ?: ""
    }

    fun getCountry(context: Context): String {
        return getPrefs(context).getString(KEY_COUNTRY, "") ?: ""
    }

    fun getCampaign(context: Context): String {
        return getPrefs(context).getString(KEY_CAMPAIGN, "") ?: ""
    }

    fun getKeyword(context: Context): String {
        return getPrefs(context).getString(KEY_KEYWORD, "") ?: ""
    }

    fun getRawReferrer(context: Context): String {
        return getPrefs(context).getString(KEY_RAW_REFERRER, "") ?: ""
    }

    fun isReferrerCaptured(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_REFERRER_CAPTURED, false)
    }
}
