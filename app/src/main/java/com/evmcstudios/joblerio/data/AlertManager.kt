package com.evmcstudios.joblerio.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class JobAlert(
    val id: String = System.currentTimeMillis().toString(),
    val query: String,
    val location: String,
    val frequency: String = "daily",
    val createdAt: Long = System.currentTimeMillis(),
    val lastNotified: Long = 0L,
    val isActive: Boolean = true
)

object AlertManager {

    private const val PREFS_NAME = "job_alerts"
    private const val KEY_ALERTS = "alerts_list"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveAlert(context: Context, alert: JobAlert) {
        val alerts = getAlerts(context).toMutableList()
        alerts.removeAll { it.query == alert.query && it.location == alert.location }
        alerts.add(0, alert)
        val json = Gson().toJson(alerts)
        getPrefs(context).edit().putString(KEY_ALERTS, json).apply()
    }

    fun removeAlert(context: Context, alertId: String) {
        val alerts = getAlerts(context).toMutableList()
        alerts.removeAll { it.id == alertId }
        val json = Gson().toJson(alerts)
        getPrefs(context).edit().putString(KEY_ALERTS, json).apply()
    }

    fun toggleAlert(context: Context, alertId: String) {
        val alerts = getAlerts(context).toMutableList()
        val index = alerts.indexOfFirst { it.id == alertId }
        if (index != -1) {
            alerts[index] = alerts[index].copy(isActive = !alerts[index].isActive)
            val json = Gson().toJson(alerts)
            getPrefs(context).edit().putString(KEY_ALERTS, json).apply()
        }
    }

    fun getAlerts(context: Context): List<JobAlert> {
        val json = getPrefs(context).getString(KEY_ALERTS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<JobAlert>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getActiveAlerts(context: Context): List<JobAlert> {
        return getAlerts(context).filter { it.isActive }
    }

    fun updateLastNotified(context: Context, alertId: String) {
        val alerts = getAlerts(context).toMutableList()
        val index = alerts.indexOfFirst { it.id == alertId }
        if (index != -1) {
            alerts[index] = alerts[index].copy(lastNotified = System.currentTimeMillis())
            val json = Gson().toJson(alerts)
            getPrefs(context).edit().putString(KEY_ALERTS, json).apply()
        }
    }
}
