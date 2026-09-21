package com.evmcstudios.joblerio

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.evmcstudios.joblerio.data.ReEngagementManager
import com.evmcstudios.joblerio.data.RemoteConfigManager
import com.google.gson.JsonObject
import kotlin.random.Random

class ReEngagementWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val enabled = RemoteConfigManager.isReEngagementEnabled()

        if (!enabled) return Result.success()

        if (!ReEngagementManager.shouldNotify(context, enabled)) {
            Log.d("ReEngagement", "Not time to notify yet")
            return Result.success()
        }

        val bracket = ReEngagementManager.getCurrentBracket(context)
        val keyword = getLastSearchedKeyword(context)
        val message = getReEngagementMessage(bracket, keyword)

        if (message.isNotBlank()) {
            NotificationHelper.showJobAlert(
                context = context,
                title = "Joblerio",
                message = message,
                notificationId = Random.nextInt(9000, 9999)
            )
            ReEngagementManager.setLastNotificationBracket(context, bracket)
            Log.d("ReEngagement", "Sent notification: $bracket - $message")
        }

        return Result.success()
    }

    private fun getLastSearchedKeyword(context: Context): String {
        val prefs = context.getSharedPreferences("home_state", Context.MODE_PRIVATE)
        return prefs.getString("search_query", "") ?: ""
    }

    private fun getReEngagementMessage(bracket: String, keyword: String): String {
        val messages = getMessagesForBracket(bracket)
        val message = messages[Random.nextInt(messages.size)]
        return if (keyword.isNotBlank() && Random.nextFloat() > 0.5f) {
            message.replace("{keyword}", keyword)
        } else {
            message.replace("{keyword}", "your area")
        }
    }

    private fun getMessagesForBracket(bracket: String): List<String> {
        return when (bracket) {
            "1h" -> listOf(
                "Looking for {keyword} jobs? New listings are waiting for you!",
                "Found new {keyword} opportunities. Come check them out!",
                "Your next job could be one tap away. See what's new!"
            )
            "3h" -> listOf(
                "The job market is active right now. Check out what's new!",
                "Don't miss out on {keyword} roles available today!",
                "New jobs matching your interests just dropped. Take a look!"
            )
            "1d" -> listOf(
                "You missed new {keyword} jobs posted today. Don't miss out!",
                "Come back and see what's changed in {keyword} jobs!",
                "Yesterday's new listings are waiting for you to explore."
            )
            "3d" -> listOf(
                "Still interested in {keyword}? We found new matches!",
                "It's been a few days - check for new {keyword} updates!",
                "The job market moves fast. See what's new in {keyword}!"
            )
            "7d" -> listOf(
                "It's been a week! New {keyword} jobs are available.",
                "Don't miss your next opportunity in {keyword}!",
                "We miss you! Come see what's changed in {keyword} jobs."
            )
            else -> emptyList()
        }
    }
}
