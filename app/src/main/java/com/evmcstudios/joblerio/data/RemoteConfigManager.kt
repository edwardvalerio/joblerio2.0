package com.evmcstudios.joblerio.data

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.tasks.await

object RemoteConfigManager {

    private const val TAG = "RemoteConfig"
    private const val CONFIG_KEY = "app_config"

    private var config: JsonObject? = null
    private val gson = Gson()

    private val defaults = mapOf(
        CONFIG_KEY to """
            {
                "jobs_api_url": "https://jobs.loyalnuggets.com/api/v1/jobs",
                "jobs_api_key": "lj_944ef823727b1daf53ec4011c195aee9dc08e9178c856940",
                "postback_url": "https://lntrk.lnuggetstrk.com/postback",
                "postback_key": "some-random-string",
                "payout": "0",
                "postback_event": "install",
                "postback_job_clicks_threshold": 2,
                "reengagement_enabled": true,
                "keyword_mapping": {
                    "US": "jobs",
                    "CA": "emploi",
                    "GB": "jobs",
                    "MX": "empleos",
                    "DE": "stellenangebote",
                    "FR": "emplois",
                    "IN": "naukri",
                    "BR": "vagas",
                    "AU": "jobs",
                    "ES": "empleo"
                }
            }
        """.trimIndent()
    )

    suspend fun init() {
        try {
            val rc = Firebase.remoteConfig
            val settings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
            rc.setConfigSettingsAsync(settings).await()
            rc.setDefaultsAsync(defaults).await()
            rc.fetchAndActivate().await()
            parseConfig(rc.getString(CONFIG_KEY))
            Log.d(TAG, "Remote config loaded: $config")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load remote config: ${e.message}")
            parseConfig(defaults[CONFIG_KEY] ?: "")
        }
    }

    private fun parseConfig(raw: String) {
        config = try {
            gson.fromJson(raw, JsonObject::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse config: ${e.message}")
            gson.fromJson(defaults[CONFIG_KEY], JsonObject::class.java)
        }
    }

    fun getPostbackUrl(): String {
        return config?.get("postback_url")?.asString
            ?: "https://lntrk.lnuggetstrk.com/postback"
    }

    fun getPostbackKey(): String {
        return config?.get("postback_key")?.asString
            ?: "some-random-string"
    }

    fun getPayout(): String {
        return config?.get("payout")?.asString ?: "0"
    }

    fun getPostbackEvent(): String {
        return config?.get("postback_event")?.asString ?: "install"
    }

    fun getPostbackClickThreshold(): Int {
        return config?.get("postback_job_clicks_threshold")?.asInt ?: 2
    }

    fun getKeywordForCountry(country: String): String {
        val mapping = config?.getAsJsonObject("keyword_mapping") ?: return "Jobs"
        val countryCode = if (country.length == 2) country.uppercase() else {
            CountryMapper.countryMap.entries.find { it.value.equals(country, ignoreCase = true) }?.key ?: ""
        }
        return mapping.get(countryCode)?.asString ?: "Jobs"
    }

    fun isReEngagementEnabled(): Boolean {
        return config?.get("reengagement_enabled")?.asBoolean ?: true
    }

    fun getJobsApiUrl(): String {
        return config?.get("jobs_api_url")?.asString
            ?: "https://jobs.loyalnuggets.com/api/v1/jobs"
    }

    fun getJobsApiKey(): String {
        return config?.get("jobs_api_key")?.asString
            ?: "lj_944ef823727b1daf53ec4011c195aee9dc08e9178c856940"
    }
}
